package com.saurav.SpringAzureOpenAI.cosmos;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.saurav.SpringAzureOpenAI.dao.Document;
import com.saurav.SpringAzureOpenAI.dao.Engineer;
import com.saurav.SpringAzureOpenAI.dao.EngineerRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EngineerService {

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatClient chatClient;

    private CosmosContainer container;

    public EngineerService(CosmosClient client) {
        this.container = client.getDatabase("AI200CosmosDB").getContainer("Engineer");
    }

    public void createEnginners(List<EngineerDTO> engineerDTOList) {
        int batchSize = 200;
        for (int i = 0; i < engineerDTOList.size(); i += batchSize) {
            List<EngineerDTO> batch = engineerDTOList.subList(i,
                    Math.min(i + batchSize, engineerDTOList.size())
            );
            List<String> profiles = batch.stream()
                    .map(EngineerDTO::profile)
                    .toList();
            List<Embedding> embeddingList = embedTexts(profiles);
            List<Engineer> engineerList = new ArrayList<>();
            for (int j = 0; j < batch.size(); j++) {
                Engineer engineer = new Engineer();
                engineer.setId(batch.get(j).id());
                engineer.setName(batch.get(j).name());
                engineer.setRole(batch.get(j).role());
                engineer.setProfile(batch.get(j).profile());
                engineer.setEmbedding(embeddingList.get(j).getOutput());
                engineerList.add(engineer);
            }
            engineerRepository.saveAll(engineerList);
        }
    }

    private List<Embedding> embedTexts(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(texts, EmbeddingOptions.builder().build());
        return embeddingModel.call(request).getResults();
    }

    private float[] embedText(String text) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text), EmbeddingOptions.builder().build());
        return embeddingModel.call(request).getResults().get(0).getOutput();
    }

    public String performRAGQuery(String query) {
        float[] embedding = embedText(query);
        List<Engineer> engineers = findTopVectorDistance(embedding);
        List<String> profileList = engineers.stream().map(Engineer::getProfile).toList();
        String context = profileList.stream().collect(Collectors.joining("\n\n"));
        return findAnswerfromModel(context,query);
    }

    private String findAnswerfromModel(String context,String query) {
        PromptTemplate prompt = new PromptTemplate("You are an expert engineer. Based on the following context, please provide a detailed answer to the user's query.\n\n" +
                "Context:\n" + context + "\n\n" +
                "Please provide your answer below:\n + The Query is : " + query);
        ChatOptions chatOptions = ChatOptions.builder().build();
        return chatClient.prompt(prompt.create(chatOptions))
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID,UUID.randomUUID()))
                .call().content();
    }

    private List<Engineer> findTopVectorDistance(float[] embedding) {

        SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT top 50 c.id,c.name,c.role,c.profile FROM c ORDER BY VectorDistance(c.embedding, @embedding)")
                .setParameters(Arrays.asList(new SqlParameter("@embedding", embedding)));
        CosmosPagedIterable<Engineer> iter = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Engineer.class);
        List<Engineer> engineers = new ArrayList<>();
        iter.forEach(engineers::add);
        Double totalRU = 0.0;
        for (FeedResponse<Engineer> page : iter.iterableByPage()) {
            totalRU += page.getRequestCharge();
        }
        System.out.println("Total RU = " + totalRU);
        return engineers;
    }
}
