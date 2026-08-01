package com.saurav.SpringAzureOpenAI.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.search.Document;

import java.util.List;
import java.util.Map;

@RestController
public class RedisEngController {

    @Autowired
    private RedisEngService redisService;

    @PostMapping("/insertDoc")
    public ResponseEntity<String> insertAllDoc(@RequestBody List<RedisEngDTO> redisEngDTOList) {

        redisService.insertEngineers(redisEngDTOList);

        return ResponseEntity.ok("Document inserted successfully!");

    }

    @GetMapping("/searchDoc")
    public ResponseEntity<String> searchDocument(@RequestHeader String query) {
        String answer = redisService.getResultfromQuery(query);
        return ResponseEntity.ok(answer);
    }
}
