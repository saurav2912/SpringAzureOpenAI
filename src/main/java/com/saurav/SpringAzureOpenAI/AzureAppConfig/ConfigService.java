package com.saurav.SpringAzureOpenAI.AzureAppConfig;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class ConfigService {

    @Autowired
    private AppConfigurationRefresh appConfigurationRefresh;

    @Autowired
    private MyProperties properties;

    public String getChatModel() {
        // Manually poke a refresh check (safe to call even with auto-refresh on)
        appConfigurationRefresh.refreshConfigurations().block();
        return properties.getChatModel();
    }

    public String getEmbedingModel() {
        // Manually poke a refresh check (safe to call even with auto-refresh on)
        appConfigurationRefresh.refreshConfigurations().block();
        return properties.getEmbedModel();
    }

    public String getImageModel() {
        // Manually poke a refresh check (safe to call even with auto-refresh on)
        appConfigurationRefresh.refreshConfigurations().block();
        return properties.getImgModel();
    }

    public String getTranscribeModel() {
        // Manually poke a refresh check (safe to call even with auto-refresh on)
        appConfigurationRefresh.refreshConfigurations().block();
        return properties.getTranscribeModel();
    }
}
