package com.saurav.SpringAzureOpenAI.postgre;

import com.saurav.SpringAzureOpenAI.dao.PGDocument;
import com.saurav.SpringAzureOpenAI.dao.PGDocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PGService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private PGDocumentRepository pgDocumentRepository;

    private ChatClient chatClient;

    public PGService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.defaultAdvisors
                (MessageChatMemoryAdvisor.builder(chatMemory).build()).build();

    }

    public void uploadDataToPostgreSQL(List<PGDocument> documents) {

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
            //pgDocumentRepository.saveAll(documents);

        }
    }

    private float[] embedText(String text) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text),EmbeddingOptions.builder().build());
        float[] embeddings = embeddingModel.call(request).getResults().get(0).getOutput();
        return embeddings;
    }

    private List<Embedding> embedText(List<String> textList) {
        EmbeddingRequest request = new EmbeddingRequest(textList,EmbeddingOptions.builder().build());
        return embeddingModel.call(request).getResults();
    }

    public List<PGDocumentDTO> retrieveData(String query) {
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(query),EmbeddingOptions.builder().build());
        float[] queryEmbedding = embeddingModel.call(request).getResults().get(0).getOutput();
        return pgDocumentRepository.findByContentContaining(queryEmbedding);
    }

}
