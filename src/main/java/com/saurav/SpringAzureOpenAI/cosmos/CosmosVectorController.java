package com.saurav.SpringAzureOpenAI.cosmos;

import com.saurav.SpringAzureOpenAI.dao.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CosmosVectorController {

    @Autowired
    private CosmosVectorService cosmosVectorService;

    @PostMapping("/upload")
    public String uploadFiletoVector(@RequestBody Document document) {
        cosmosVectorService.uploadFiletoVector(document);
        return "Document uploaded successfully!";
    }

    @PostMapping("/upload/multi")
    public String uploadMultiFiletoVector(@RequestBody List<Document> documents) {
        cosmosVectorService.uploadFiletoVector(documents);
        return "Document uploaded successfully!";
    }

    @GetMapping("/getDocuments")
    public List<Document> getDocuments(@RequestHeader String query) {
        return cosmosVectorService.getAllDocuments(query);
    }
}
