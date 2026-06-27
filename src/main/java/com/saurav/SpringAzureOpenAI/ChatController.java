package com.saurav.SpringAzureOpenAI;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/classify")
    public ResponseEntity<String> classify(@RequestHeader String topic) throws IOException {
        String sessionId = (String) session.getAttribute("sessionId");
        String response = azureAIService.classify(topic,sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generateImage")
    public ResponseEntity<String> generateImgage(@RequestBody String prompt, @RequestHeader String name) throws IOException {
        azureAIService.generateImage(prompt,name);
        return ResponseEntity.ok("Image generated");
    }

    @PostMapping("/embedText")
    public ResponseEntity<float[]> embed(@RequestHeader String text) {
        float[] response  = azureAIService.getEmbedding(text);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findSimilarity")
    public ResponseEntity<Double> findSimilarity(@RequestHeader String text) {
        double response  = azureAIService.findSimilarity(text);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findPlagiarism")
    public ResponseEntity<Double> findPlagiarism(@RequestHeader String docName) throws IOException {
        double response  = azureAIService.findPlagiarism(docName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcriptAudio(@RequestBody MultipartFile file) throws IOException {
        String response = azureAIService.convertToText(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate")
    public ResponseEntity<String> translateAudio(@RequestBody MultipartFile file) throws IOException {
        String response = azureAIService.translate(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summerize")
    public ResponseEntity<String> summerizeAudio(@RequestBody MultipartFile file) throws IOException {
        String response = azureAIService.summerize(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generateQuiz")
    public ResponseEntity<String> summerizeQuiz(@RequestBody MultipartFile file) throws IOException {
        String response = azureAIService.generateQuiz(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/queryRAG")
    public ResponseEntity<String> getResponsefromVector(@RequestBody String query) throws IOException {
        String response = azureAIService.retrieveFromVector(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fineTune")
    public ResponseEntity<String> getTunedAnswer(@RequestBody String query) throws IOException {
        String response = azureAIService.fetchTunedAnswer(query);
        return ResponseEntity.ok(response);
    }
}
