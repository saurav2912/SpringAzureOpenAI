package com.saurav.SpringAzureOpenAI;

import com.openai.models.images.ImagesResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;

@Service
public class AzureAIService {

    @Autowired
    public ImageModel imageModel;

    @Autowired
    public EmbeddingModel embeddingModel;

    private ChatClient chatClient;

    public AzureAIService(ChatClient.Builder builder, ChatMemory chatMemory){
        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    public String getAnswer(String prompt, String id) throws IOException {

        return chatClient.prompt(prompt)
                .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, id))
                .system("You are a lyric writer")
                .call().content();
    }

    public void generateImage(String prompt,String name) throws IOException {
        ImageOptions options = OpenAiImageOptions.builder()
                .azure(true)
                .model("sauravazimage")
                .height(1024)
                .width(1024)
                .quality(ImagesResponse.Quality.MEDIUM.toString())
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(prompt,options);
        String image = imageModel.call(imagePrompt).getResult().getOutput().getB64Json();
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(name);
        Files.write(path, Base64.getDecoder().decode(image), StandardOpenOption.CREATE);
    }

    public String classify(String topic, String sessionId) throws IOException {
        String fileName = "";
        switch (topic) {
            case "Product":
                fileName = "ProductLaunch.txt";
                break;
            case "Movie":
                fileName = "MovieReview.txt";
                break;
            case "Politics":
                fileName = "PoliticalEvent.txt";
                break;
            case "Tech":
                fileName = "TechConference.txt";
                break;
            case "Shopping":
                fileName = "HolidayShopping.txt";
                break;
        }


        String prompt = Files.readString(Path.of(new ClassPathResource(fileName).getURI()));
        prompt = "Please classify the below statements: "+prompt;
        return chatClient.prompt(prompt)
                .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call().content();
    }

    public float[] getEmbedding(String text){
        EmbeddingOptions options = EmbeddingOptions.builder().model("sauravazembedding").build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text),options);
        return embeddingModel.call(request).getResults().get(0).getOutput();
    }

    public double findSimilarity(String text) {
        EmbeddingOptions options = EmbeddingOptions.builder().model("sauravazembedding").build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text.split(",")),options);
        float [] f1 = embeddingModel.call(request).getResults().get(0).getOutput();
        float [] f2 = embeddingModel.call(request).getResults().get(1).getOutput();
        return CosSimilarity(f1,f2);
    }

    public double CosSimilarity(float[] f1, float[] f2) {
        double dot=0,mod1=0,mod2=0;

        for(int i=0;i<f1.length;i++) {
            dot += f1[i]*f2[i];
            mod1 += f1[i]*f1[i];
            mod2 += f2[i]*f2[i];
        }

        return dot/(Math.sqrt(mod1)*Math.sqrt(mod2));
    }
}
