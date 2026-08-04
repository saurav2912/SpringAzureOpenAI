package com.saurav.SpringAzureOpenAI.ServiceBus;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SbusConfig {

    @Value("${azure.mi}")
    private String managedIdentity;

    @Value("${kv.url}")
    private String kvUrl;

    @Value("${sbus.queue}")
    private String queueName;

    @Value("${sbus.topic1}")
    private String topic1;

    @Value("${sbus.topic2}")
    private String topic2;

    @Value("${sbus.topic1.sub1}")
    private String topic1sub1;

    @Value("${sbus.topic1.sub2}")
    private String topic1sub2;

    @Value("${sbus.topic2.sub1}")
    private String topic2sub1;



    @Bean
    public ServiceBusClientBuilder builder(){
        return new ServiceBusClientBuilder();
    }

    @Bean("QueueSender")
    public ServiceBusSenderClient buildQueueSenderClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .sender().queueName(queueName)
                .buildClient();
    }

    @Bean("QueueReceiver")
    public ServiceBusReceiverClient buildQueueReceiverClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().queueName(queueName)
                .buildClient();
    }

    @Bean("DLQReceiver")
    public ServiceBusReceiverClient buildDLQReceiverClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().queueName(queueName).subQueue(SubQueue.DEAD_LETTER_QUEUE)
                .buildClient();
    }

    @Bean("Topic1Sender")
    public ServiceBusSenderClient buildTopic1SenderClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .sender().topicName(topic1)
                .buildClient();
    }

    @Bean("Topic1Sub1Receiver")
    public ServiceBusReceiverClient buildTopic1Sub1ReceiverClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().topicName(topic1).subscriptionName(topic1sub1)
                .buildClient();
    }

    @Bean("Topic1Sub1ReceiverDLQ")
    public ServiceBusReceiverClient buildTopic1Sub1ReceiverDLQClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().topicName(topic1).subscriptionName(topic1sub1).subQueue(SubQueue.DEAD_LETTER_QUEUE)
                .buildClient();
    }

    @Bean("Topic1Sub2Receiver")
    public ServiceBusReceiverClient buildTopic1Sub2ReceiverClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().topicName(topic1).subscriptionName(topic1sub2)
                .buildClient();
    }

    @Bean("Topic2Sender")
    public ServiceBusSenderClient buildTopic2SenderClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .sender().topicName(topic2)
                .buildClient();
    }

    @Bean("Topic2Sub1Receiver")
    public ServiceBusReceiverClient buildTopic2Sub1ReceiverClient(ServiceBusClientBuilder builder){
        return builder.connectionString(getConnectionString())
                .receiver().topicName(topic2).subscriptionName(topic2sub1)
                .buildClient();
    }



    private String getConnectionString(){
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .managedIdentityClientId(managedIdentity).build();
        SecretClient client = new SecretClientBuilder()
                .vaultUrl(kvUrl).credential(credential).buildClient();
        KeyVaultSecret secret = client.getSecret("sbusConnectionString");
        return secret.getValue();
    }
}
