package com.saurav.SpringAzureOpenAI.KeyVault;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KVConfig {

    @Value("${azure.mi}")
    private String managedIdentity;

    @Value("${kv.url}")
    private String kvUrl;

    @Bean
    public SecretClient buildSecretClient() {
        SecretClientBuilder builder = new SecretClientBuilder();
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().
                managedIdentityClientId(managedIdentity).build();
        return builder.credential(credential).vaultUrl(kvUrl).buildClient();
    }
}
