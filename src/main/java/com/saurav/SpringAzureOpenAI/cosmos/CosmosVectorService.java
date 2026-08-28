package com.saurav.SpringAzureOpenAI.cosmos;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.saurav.SpringAzureOpenAI.AzureAppConfig.ConfigService;
import com.saurav.SpringAzureOpenAI.dao.Document;
import com.saurav.SpringAzureOpenAI.dao.DocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class CosmosVectorService {

    private final CosmosContainer container;
    private final CosmosAsyncContainer asyncContainer;
    private final CosmosAsyncContainer leaseContainer;

    public CosmosVectorService(
            CosmosAsyncClient cosmosAsyncClient,
            CosmosClient cosmosClient) {


        CosmosDatabase database =
                cosmosClient.getDatabase("AI200CosmosDB");

        this.container =
                database.getContainer("document");
        this.asyncContainer =
                cosmosAsyncClient.getDatabase("AI200CosmosDB")
                        .getContainer("document");
        this.leaseContainer =
                cosmosAsyncClient.getDatabase("AI200CosmosDB")
                        .getContainer("leases");
    }

    @Autowired
    private ChatClient chatClient;

    /*@PostConstruct
    private void init() {
        // Create the lease container if it doesn't exist
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName("my-document-host")
                .feedContainer(asyncContainer)
                .leaseContainer(leaseContainer)
                .handleChanges(changes -> {
                    for (JsonNode document : changes) {
                        System.out.println("Change detected: " + document);
                    }
                })
                .buildChangeFeedProcessor();
        changeFeedProcessor.start().block();
    }*/

    private static final String SIMILARITY_QUERY = "Select top 5 c.id,c.title,c.content,c.category from c " +
            "order by VectorDistance(c.vector, @vector)";
    private static final String FULLTEXT_SEARCH_QUERY="SELECT TOP 5 c.id,c.title,c.content,c.category" +
            " FROM c  ORDER BY RANK FullTextScore(c.content, @Query)";
    private static final String HYBRID_SEARCH_QUERY="SELECT TOP 5 c.id,c.title,c.content,c.category" +
            " FROM c  ORDER BY RANK RRF(VectorDistance(c.vector, @vector),FullTextScore(c.content, @Query)) ";
    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DocumentRepository documentRepository;

    private float[] getEmbedding(String text) {
        EmbeddingOptions options = EmbeddingOptions.builder()
                .model(configService.getEmbedingModel())
                .build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text), options);
        return embeddingModel.call(request).
                getResults().get(0).getOutput();
    }
    public void uploadFiletoVector(Document document) {
        float[] vectorArray = getEmbedding(document.getContent());
        document.setVector(IntStream.range(0, vectorArray.length).mapToObj(i -> vectorArray[i]).toList());
        documentRepository.save(document);
    }

    public void uploadFiletoVector(List<Document> documents) {
        documents.forEach(x->{
            float[] vectorArray = getEmbedding(x.getContent());
            x.setVector(IntStream.range(0, vectorArray.length).mapToObj(i -> vectorArray[i]).toList());
        });

        documentRepository.saveAll(documents);
    }

    public List<Document> getAllDocuments(String query) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(query),
                OpenAiEmbeddingOptions.builder().model(configService.getEmbedingModel()).build());
        float[] queryEmbedding = embeddingModel.call(request).getResults().get(0).getOutput();
        List<Float> queryVector = IntStream.range(0, queryEmbedding.length).mapToObj(i -> queryEmbedding[i]).toList();
        return findByVectorSimilarity(queryVector);
    }

    private List<Document> findByVectorSimilarity(List<Float> queryVector) {
        List<Document> docs = new ArrayList<>();
        SqlQuerySpec querySpec = new SqlQuerySpec(SIMILARITY_QUERY)
                .setParameters(Arrays.asList(new SqlParameter("@vector", queryVector)));
        Double totalRU = 0.0;
        CosmosPagedIterable<Document> iterable =
                container.queryItems(
                        querySpec,
                        new CosmosQueryRequestOptions(),
                        Document.class);
        for (FeedResponse<Document> page : iterable.iterableByPage()) {
            totalRU += page.getRequestCharge();
        }
        System.out.println("Total RU = " + totalRU);
        iterable.forEach(docs::add);
        return docs;
    }

    public List<Document> findByFulltextSearch(String query) {
        List<Document> docs = new ArrayList<>();
        SqlQuerySpec querySpec = new SqlQuerySpec(FULLTEXT_SEARCH_QUERY)
                .setParameters(Arrays.asList(new SqlParameter("@Query", query)));
        Double totalRU = 0.0;
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(true);
        //options.setPartitionKey(new PartitionKey("/id"));
        CosmosPagedIterable<Document> iterable =
                container.queryItems(
                        querySpec,
                        options,
                        Document.class);
        for (FeedResponse<Document> page : iterable.iterableByPage()) {
            totalRU += page.getRequestCharge();
            docs.addAll(page.getResults());
        }
        System.out.println("Total RU = " + totalRU);
        //iterable.forEach(docs::add);
        return docs;
    }

    public List<Document> findByHybridSearch(String query) {
        List<Document> docs = new ArrayList<>();
        float[] vector = getEmbedding(query);
        SqlQuerySpec querySpec = new SqlQuerySpec(HYBRID_SEARCH_QUERY)
                .setParameters(Arrays.asList(new SqlParameter("@Query", query),
                        new SqlParameter("@vector",vector)));
        Double totalRU = 0.0;
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setQueryMetricsEnabled(true);
        options.setMaxDegreeOfParallelism(-1);
        //options.setPartitionKey(new PartitionKey("/id"));
        CosmosPagedIterable<Document> iterable =
                container.queryItems(
                        querySpec,
                        options,
                        Document.class);
        for (FeedResponse<Document> page : iterable.iterableByPage()) {
            totalRU += page.getRequestCharge();
            docs.addAll(page.getResults());
        }
        System.out.println("Total RU = " + totalRU);
        //iterable.forEach(docs::add);
        return docs;
    }

    public String processRAGBot(String query) {
        List<Document> docs = findByHybridSearch(query);
        ChatOptions options = OpenAiChatOptions.builder()
                .model(configService.getChatModel())
                .build();

        String template = "You are an AI expert in Azure Java cloud technologies." +
                " Please find best response from the below text" + docs.stream().map(x->x.getContent()).toList().toString();
       String response =chatModel.call(new Prompt(template,options)).getResult().getOutput().getText();
       return response;
    }
}
