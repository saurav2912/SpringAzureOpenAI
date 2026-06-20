package com.saurav.SpringAzureOpenAI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureAIService {

    private ChatClient chatClient;

    public AzureAIService(ChatClient.Builder builder, ChatMemory chatMemory){
        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    public String getAnswer(String prompt, String id) throws IOException {
        return chatClient.prompt(prompt)
                .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, id))
                .call().content();
    }
}
