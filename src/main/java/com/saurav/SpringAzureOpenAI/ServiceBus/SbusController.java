package com.saurav.SpringAzureOpenAI.ServiceBus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SbusController {

    @Autowired
    private SbusService sbusService;

    @PostMapping("queue/send")
    public ResponseEntity<String> sendMessageToQueue(@RequestBody List<String> messages){
        if(messages.size()>1)
            sbusService.sendMessageBatchToQueue(messages);
        else
            sbusService.sendMessageToQueue(messages.get(0));
        return ResponseEntity.ok("Success");
    }

    @GetMapping("queue/get/{size}")
    public ResponseEntity<List<Object>> fetchMessageFromQueue(@PathVariable int size){
        List<Object> list = sbusService.peekMessagesFromQueue(size);
        return ResponseEntity.ok(list);
    }

    @GetMapping("queue/receive/{size}")
    public ResponseEntity<List<Object>> receiveMessageFromQueue(@PathVariable int size,@RequestHeader(required = false) boolean dlq){
        List<Object> list = sbusService.receiveMessagesFromQueue(size,dlq);
        return ResponseEntity.ok(list);
    }

    @GetMapping("queue/complete/{size}")
    public ResponseEntity<String> completeMessageFromQueue(@PathVariable int size,@RequestHeader(required = false) boolean dlq){
        sbusService.completeMessagesFromQueue(size,dlq);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("topic/send")
    public ResponseEntity<String> sendMessageToTopic(@RequestBody List<String> messages,@RequestHeader String topic){
        if(messages.size()>1)
            sbusService.sendMessageBatchToTopic(messages,topic);
        else
            sbusService.sendMessageToTopic(messages.get(0),topic);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("topic/get/{size}")
    public ResponseEntity<List<Object>> fetchMessageFromQueue(@PathVariable int size,
                                                              @RequestHeader String topic,
                                                              @RequestHeader String subscription){
        List<Object> list = sbusService.peekMessagesFromTopic(size,topic,subscription);
        return ResponseEntity.ok(list);
    }

    @GetMapping("topic/receive/{size}")
    public ResponseEntity<List<Object>> receiveMessageFromQueue(@PathVariable int size,@RequestHeader String topic,
                                                                @RequestHeader String subscription,
                                                                @RequestHeader(required = false) boolean dlq){
        List<Object> list = sbusService.receiveMessagesFromTopic(size,topic,subscription,dlq);
        return ResponseEntity.ok(list);
    }

    @GetMapping("topic/complete/{size}")
    public ResponseEntity<String> completeMessageFromTopic(@PathVariable int size,@RequestHeader String topic,
                                                           @RequestHeader String subscription,
                                                           @RequestHeader(required = false) boolean dlq){
        sbusService.completeMessagesFromTopic(size,topic,subscription,dlq);
        return ResponseEntity.ok("Success");
    }
}
