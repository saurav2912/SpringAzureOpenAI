package com.saurav.SpringAzureOpenAI.KeyVault;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KVController {

    @Autowired
    private SecretClient secretClient;

    @PostMapping("/insert/kv")
    public void insertSecret(@RequestHeader String key, @RequestHeader String value) {
        secretClient.setSecret(key, value);
    }

    @PostMapping("/get/kv")
    public String getSecret(@RequestHeader String key) {
        return secretClient.getSecret(key).getValue();
    }
}
