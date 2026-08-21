package com.saurav.SpringAzureOpenAI.AzureAppConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class MyProperties {
    private String chatModel;
    private String embedModel;
    private String imgModel;
    private String transcribeModel;


}
