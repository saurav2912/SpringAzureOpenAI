package com.saurav.SpringAzureOpenAI.ServiceBus;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SbusService {

    @Value("${sbus.queue}")
    private String queueName;

    @Value("${azure.mi}")
    private String managedIdentity;

    @Value("${kv.url}")
    private String kvUrl;

    @Autowired
    @Qualifier("QueueSender")
    private ServiceBusSenderClient serviceBusQueueSenderClient;

    @Autowired
    @Qualifier("QueueReceiver")
    private ServiceBusReceiverClient serviceBusQueueReceiverClient;

    @Autowired
    @Qualifier("DLQReceiver")
    private ServiceBusReceiverClient serviceBusDLQReceiverClient;

    @Autowired
    @Qualifier("Topic1Sender")
    private ServiceBusSenderClient serviceBusTopic1SenderClient;

    @Autowired
    @Qualifier("Topic1Sub1Receiver")
    private ServiceBusReceiverClient serviceBusTopic1Sub1ReceiverClient;

    @Autowired
    @Qualifier("Topic1Sub2Receiver")
    private ServiceBusReceiverClient serviceBusTopic1Sub2ReceiverClient;

    @Autowired
    @Qualifier("Topic2Sender")
    private ServiceBusSenderClient serviceBusTopic2SenderClient;

    @Autowired
    @Qualifier("Topic2Sub1Receiver")
    private ServiceBusReceiverClient serviceBusTopic2Sub1ReceiverClient;

    @Autowired
    @Qualifier("Topic1Sub1ReceiverDLQ")
    private ServiceBusReceiverClient serviceBusTopic1Sub1ReceiverDLQClient;


    public void sendMessageToQueue(String message) {
        ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
        serviceBusQueueSenderClient.sendMessage(serviceBusMessage);

    }

    public void sendMessageBatchToQueue(List<String> messages){
        ServiceBusMessageBatch batch = serviceBusQueueSenderClient.createMessageBatch();
        messages.forEach(message -> {
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            batch.tryAddMessage(serviceBusMessage);
        });
        serviceBusQueueSenderClient.sendMessages(batch);
    }

    public List<Object> peekMessagesFromQueue(int size) {
        List<Object> list = new ArrayList<>();
        Iterable<ServiceBusReceivedMessage> messages = serviceBusQueueReceiverClient.peekMessages(size);
        messages.forEach(m->{
            list.add( m.getBody().toString());
        });
        return list;
    }

    public List<Object> receiveMessagesFromQueue(int size,boolean dlq) {
        List<Object> list = new ArrayList<>();
        Iterable<ServiceBusReceivedMessage> messages = serviceBusQueueReceiverClient.receiveMessages(size, Duration.ofSeconds(10));
        if(!messages.iterator().hasNext() && dlq) {
            messages = serviceBusDLQReceiverClient.receiveMessages(size, Duration.ofSeconds(10));
        }
        messages.forEach(m->{
            list.add( m.getBody().toString());
            serviceBusQueueReceiverClient.deadLetter(m);
        });
        return list;
    }

    public void completeMessagesFromQueue(int size,boolean dlq) {
        Iterable<ServiceBusReceivedMessage> messages = serviceBusQueueReceiverClient.receiveMessages(size,Duration.ofSeconds(10));
        if(!messages.iterator().hasNext() && dlq) {
            messages = serviceBusDLQReceiverClient.receiveMessages(size, Duration.ofSeconds(10));
            messages.forEach(m->{
                serviceBusDLQReceiverClient.complete(m);
            });
        } else {
            messages.forEach(m->{
                serviceBusQueueReceiverClient.complete(m);
            });
        }

    }

    private String getConnectionString(){
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .managedIdentityClientId(managedIdentity).build();
        SecretClient client = new SecretClientBuilder()
                .vaultUrl(kvUrl).credential(credential).buildClient();
        KeyVaultSecret secret = client.getSecret("queueSendListnenConString");
        return secret.getValue();
    }

    public void sendMessageToTopic(String message,String topic) {
        ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
        serviceBusMessage.setMessageId(UUID.randomUUID().toString());
        if(topic.equalsIgnoreCase("top1"))
            serviceBusTopic1SenderClient.sendMessage(serviceBusMessage);
        else
            serviceBusTopic2SenderClient.sendMessage(serviceBusMessage);
    }

    public void sendMessageBatchToTopic(List<String> messages,String topic){
        String partitionKey = "Batch-"+UUID.randomUUID();
        AtomicReference<ServiceBusMessageBatch> batchAtomicReference = new AtomicReference<>();
        if(topic.equalsIgnoreCase("top1"))
            batchAtomicReference.set(serviceBusTopic1SenderClient.createMessageBatch());
        else
            batchAtomicReference.set(serviceBusTopic2SenderClient.createMessageBatch());
        messages.forEach(message -> {
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
            serviceBusMessage.setMessageId(UUID.randomUUID().toString());
            serviceBusMessage.setPartitionKey(partitionKey);
            batchAtomicReference.get().tryAddMessage(serviceBusMessage);
        });
        if(topic.equalsIgnoreCase("top1"))
            serviceBusTopic1SenderClient.sendMessages(batchAtomicReference.get());
        else
            serviceBusTopic2SenderClient.sendMessages(batchAtomicReference.get());

    }

    public List<Object> peekMessagesFromTopic(int size,String topic,String subscription) {
        List<Object> list = new ArrayList<>();
        Iterable<ServiceBusReceivedMessage> messages = null;
        ServiceBusReceiverClient serviceBusReceiverClient = getTopicReceiver(topic,subscription);
        messages = serviceBusReceiverClient.peekMessages(size);
        messages.forEach(m->{
            list.add( m.getBody().toString());
        });
        return list;
    }

    public List<Object> receiveMessagesFromTopic(int size,String topic,String subscription,boolean dlq) {
        List<Object> list = new ArrayList<>();
        Iterable<ServiceBusReceivedMessage> messages = null;
        ServiceBusReceiverClient serviceBusReceiverClient = getTopicReceiver(topic,subscription);
        messages = serviceBusReceiverClient.receiveMessages(size,Duration.ofSeconds(5));
        if(!messages.iterator().hasNext() && dlq) {
            messages = serviceBusTopic1Sub1ReceiverDLQClient.receiveMessages(size, Duration.ofSeconds(5));
        }
        messages.forEach(m->{
            list.add( m.getBody().toString());
            //serviceBusQueueReceiverClient.deadLetter(m);
        });
        return list;
    }

    public void completeMessagesFromTopic(int size,String topic,String subscription,boolean dlq) {
        Iterable<ServiceBusReceivedMessage> messages = null;
        ServiceBusReceiverClient serviceBusReceiverClient = getTopicReceiver(topic,subscription);
        messages = serviceBusReceiverClient.receiveMessages(size, Duration.ofSeconds(5));


        if(!messages.iterator().hasNext() && dlq) {
            messages = serviceBusTopic1Sub1ReceiverDLQClient.receiveMessages(size, Duration.ofSeconds(5));
            messages.forEach(m->{
                serviceBusTopic1Sub1ReceiverDLQClient.complete(m);
            });
        } else {
            messages.forEach(m->{
                serviceBusReceiverClient.complete(m);
            });
        }

    }
    private ServiceBusReceiverClient getTopicReceiver(String topic,String subscription) {
        ServiceBusReceiverClient serviceBusReceiverClient;
        if(topic.equalsIgnoreCase("top1") && subscription.equalsIgnoreCase("sub1"))
            serviceBusReceiverClient = serviceBusTopic1Sub1ReceiverClient;
        else if (topic.equalsIgnoreCase("top1") && subscription.equalsIgnoreCase("sub2"))
            serviceBusReceiverClient = serviceBusTopic1Sub2ReceiverClient;
        else
            serviceBusReceiverClient = serviceBusTopic2Sub1ReceiverClient;
        return serviceBusReceiverClient;
    }

}
