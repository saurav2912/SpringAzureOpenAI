package com.saurav.SpringAzureOpenAI.AzureAppConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/get/chat")
    public String getChatModel() {
        return "Model: " +configService.getChatModel();
    }

    @GetMapping("/get/embed")
    public String getEmbedingModel() {
        return "Model: " + configService.getEmbedingModel();
    }

    @GetMapping("/get/image")
    public String getImageModel() {
        return "Model: " + configService.getImageModel();
    }

    @GetMapping("/get/transcribe")
    public String getTranscribeModel() {
        return "Model: " + configService.getTranscribeModel();
    }



}
