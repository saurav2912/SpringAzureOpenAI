package com.saurav.SpringAzureOpenAI.postgre;

import com.saurav.SpringAzureOpenAI.dao.PGDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PGController {

    @Autowired
    private PGService pgService;

    @PostMapping("/uploadData")
    public String uploadDataToPostgreSQL(@RequestBody List<PGDocument> documents) {
        pgService.uploadDataToPostgreSQL(documents);
        return "Data uploaded to PostgreSQL successfully!";
    }

    @PostMapping("/retriveForQuery")
    public List<PGDocumentDTO> uploadDataToPostgreSQL(@RequestBody String  query) {
        return pgService.retrieveData(query);
    }
}
