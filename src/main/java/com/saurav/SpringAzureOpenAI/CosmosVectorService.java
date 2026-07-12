package com.saurav.SpringAzureOpenAI;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.saurav.SpringAzureOpenAI.dao.Document;
import com.saurav.SpringAzureOpenAI.dao.DocumentRepository;
import com.saurav.SpringAzureOpenAI.dao.Employee;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class CosmosVectorService {

    private final CosmosContainer container;
    private final CosmosAsyncContainer asyncContainer;
    private final CosmosAsyncContainer leaseContainer;

    public CosmosVectorService(
            CosmosAsyncClient cosmosAsyncClient,
            CosmosClient cosmosClient) {

        //this.cosmosTemplate = cosmosTemplate;

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

    @PostConstruct
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
    }

    private static final String SIMILARITY_QUERY = "Select top 5 c.id,c.title,c.content,c.category from c " +
            "order by VectorDistance(c.vector, @vector)";

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DocumentRepository documentRepository;

    private float[] getEmbedding(String text) {
        EmbeddingOptions options = EmbeddingOptions.builder()
                .build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text), options);
        return embeddingModel.call(request).getResults().get(0).getOutput();
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
                EmbeddingOptions.builder().build());
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
}
