package com.saurav.SpringAzureOpenAI;

import com.openai.models.images.ImagesResponse;
import io.netty.util.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
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

    /*@Autowired
    public OpenAiImageModel imageModel;*/

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
}
