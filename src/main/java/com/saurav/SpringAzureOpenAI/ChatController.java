package com.saurav.SpringAzureOpenAI;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ChatController {

    @Autowired
    private HttpSession session;

    @Autowired
    private AzureAIService azureAIService;

    @PostMapping("/getAnswer")
    public ResponseEntity<String> getAnswer(@RequestBody String prompt) throws IOException {
        String sessionId = (String) session.getAttribute("sessionId");
        String response = azureAIService.getAnswer(prompt,sessionId);
        return ResponseEntity.ok(response);  
    }
}
