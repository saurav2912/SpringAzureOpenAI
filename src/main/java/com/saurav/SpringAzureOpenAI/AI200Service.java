package com.saurav.SpringAzureOpenAI;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.saurav.SpringAzureOpenAI.AzureAppConfig.ConfigService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AI200Service {

    @Autowired
    FeatureManager featureManager;

    @Autowired
    private ConfigService configService;

    @Value("${app.ai.openai.base-url}")
    private String openAPIUrl;
    @Value("${app.ai.openai.api-key}")
    private String openAPIKey;
    @Value("${app.ai.azure.foundry.base-url}")
    private String azFoundryAPIUrl;
    @Value("${app.ai.azure.foundry.api-key}")
    private String azFoundryAPIKey;

    public ChatModel getChatModel() {
        OpenAiChatOptions options;
        if(featureManager.isEnabled("aiFeature")) {
            options = OpenAiChatOptions.builder()
                    .baseUrl(azFoundryAPIUrl)
                    .apiKey(azFoundryAPIKey)
                    .azure(true)
                    .model("sauravaz-gpt5.2")
                    .build();
        } else {
            options = OpenAiChatOptions.builder()
                    .baseUrl(openAPIUrl)
                    .apiKey(openAPIKey)
                    .model(configService.getChatModel())
                    .build();
        }
        return OpenAiChatModel.builder().options(options).build();
    }

    public EmbeddingModel getEmbedModel() {
        OpenAiEmbeddingOptions options;
        if(featureManager.isEnabled("aiFeature")) {
            options = OpenAiEmbeddingOptions.builder()
                    .baseUrl(azFoundryAPIUrl)
                    .apiKey(azFoundryAPIKey)
                    .azure(true)
                    .model("sauravazembed-adda02")
                    .build();
        } else {
            options = OpenAiEmbeddingOptions.builder()
                    .baseUrl(openAPIUrl)
                    .apiKey(openAPIKey)
                    .model(configService.getEmbedingModel())
                    .build();
        }
        return OpenAiEmbeddingModel.builder().options(options).build();
    }



}
