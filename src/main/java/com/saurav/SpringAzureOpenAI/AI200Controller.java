package com.saurav.SpringAzureOpenAI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AI200Controller {

    @GetMapping("/ai200")
    public ResponseEntity<String> getAI200() {

        return ResponseEntity.ok("AI200 is running Successfully!");
    }
}
