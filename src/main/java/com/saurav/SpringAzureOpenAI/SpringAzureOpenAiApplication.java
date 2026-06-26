package com.saurav.SpringAzureOpenAI;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.azure.AzureVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SpringAzureOpenAiApplication {

	public static void main(String[] args) {

        SpringApplication.run(SpringAzureOpenAiApplication.class, args);
	}

    @Bean
    public SearchIndexClient searchIndexClient() {
        return new SearchIndexClientBuilder().endpoint(System.getenv("AZURE_AI_SEARCH_ENDPOINT"))
                .credential(new AzureKeyCredential(System.getenv("AZURE_AI_SEARCH_API_KEY")))
                .buildClient();
    }

    @Bean
    public VectorStore vectorStore(SearchIndexClient searchIndexClient, EmbeddingModel embeddingModel) {

        return AzureVectorStore.builder(searchIndexClient, embeddingModel)
                .initializeSchema(false)
                // Define the metadata fields to be used
                // in the similarity search filters.
                .defaultTopK(5)
                .defaultSimilarityThreshold(0.7)
                .indexName("smpaz-doc-index")
                .build();
    }


}
