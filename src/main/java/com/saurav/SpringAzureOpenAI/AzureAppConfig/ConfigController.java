package com.saurav.SpringAzureOpenAI.AzureAppConfig;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @Autowired
    private AppConfigurationRefresh appConfigurationRefresh;

    @Autowired
    private MyProperties properties;

   /* @Value("${application.version}")
    private String version;*/

    @GetMapping("/get")
    public String home() {
        // Manually poke a refresh check (safe to call even with auto-refresh on)
        appConfigurationRefresh.refreshConfigurations().block();
        return "Model: " + properties.getChatModel();
    }



}
