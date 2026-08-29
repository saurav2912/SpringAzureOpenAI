package com.saurav.SpringAzureOpenAI.postgre;

import com.saurav.SpringAzureOpenAI.AzureAppConfig.ConfigService;
import com.saurav.SpringAzureOpenAI.dao.PGDocument;
import com.saurav.SpringAzureOpenAI.dao.PGDocumentRepository;
import com.saurav.SpringAzureOpenAI.dao.PGEngineer;
import com.saurav.SpringAzureOpenAI.dao.PGEngineerRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.*;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PGService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ConfigService configService;

    @Autowired
    private PGDocumentRepository pgDocumentRepository;

    @Autowired
    private PGEngineerRepository pgEngineerRepository;

    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    public PGService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.defaultAdvisors
                (MessageChatMemoryAdvisor.builder(chatMemory).build()).build();

    }

    public void uploadDocumentDataToPostgreSQL(List<PGDocument> documents) {

        int batchSize = 200;
        for (int i = 0; i < documents.size(); i += batchSize) {

            List<PGDocument> batch = documents.subList(
                    i,
                    Math.min(i + batchSize, documents.size())
            );

            List<String> texts = batch.stream()
                    .map(PGDocument::getContent)
                    .toList();

            List<Embedding> embeddings = embedText(texts);

            for (int j = 0; j < batch.size(); j++) {
                batch.get(j).setEmbedding(
                        embeddings.get(j).getOutput()
                );
            }

            pgDocumentRepository.saveAll(batch);

        }
    }

    public void uploadEngineerDataToPostgreSQL(List<PGEngineer> engineers) {

        int batchSize = 200;
        for (int i = 0; i < engineers.size(); i += batchSize) {

            List<PGEngineer> batch = engineers.subList(
                    i,
                    Math.min(i + batchSize, engineers.size())
            );

            List<String> texts = batch.stream()
                    .map(PGEngineer::getProfile)
                    .toList();

            List<Embedding> embeddings = embedText(texts);

            for (int j = 0; j < batch.size(); j++) {
                batch.get(j).setEmbedding(
                        embeddings.get(j).getOutput()
                );
            }

            pgEngineerRepository.saveAll(batch);

        }
    }

    private float[] embedText(String text) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text),buildEmbeddingOption());
        float[] embeddings = embeddingModel.call(request).getResults().get(0).getOutput();
        return embeddings;
    }

    private List<Embedding> embedText(List<String> textList) {
        EmbeddingRequest request = new EmbeddingRequest(textList,buildEmbeddingOption());
        return embeddingModel.call(request).getResults();
    }

    public List<PGDocumentDTO> retrieveDocument(String query) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(query),buildEmbeddingOption());
        float[] queryEmbedding = embeddingModel.call(request).getResults().get(0).getOutput();
        return pgDocumentRepository.findByContentContaining(queryEmbedding);
    }

    public List<PGEngineerDTO> retrieveEngineers(String query) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(query),buildEmbeddingOption());
        float[] queryEmbedding = embeddingModel.call(request).getResults().get(0).getOutput();
        return pgEngineerRepository.findByContentContaining(queryEmbedding);
    }

    private EmbeddingOptions buildEmbeddingOption() {
        EmbeddingOptions options = OpenAiEmbeddingOptions
                .builder()
                .model(configService.getEmbedingModel())
                .build();
        return options;
    }

    private ChatOptions buildChatoptions(){
        ChatOptions options = OpenAiChatOptions
                .builder()
                .model(configService.getChatModel())
                .build();
        return options;
    }

    public String getRAGforDoc(String query) {
        List<PGDocumentDTO> docs = retrieveDocument(query);
        String contents = docs.stream().map(PGDocumentDTO::content).collect(Collectors.joining(","));
        return chatModel.call(new Prompt("Please answer for the query: "+query+" from the below contents :"+contents,
                buildChatoptions()))
                .getResult().getOutput().getText();

    }

    public String getRAGforEmp(String query) {
        List<PGEngineerDTO> docs = retrieveEngineers(query);
        String contents = docs.stream().map(PGEngineerDTO::profile).collect(Collectors.joining(","));
        return chatModel.call(new Prompt("Please answer for the query: "+query+" from the below contents :"+contents,
                        buildChatoptions()))
                .getResult().getOutput().getText();
    }
}
