package com.saurav.SpringAzureOpenAI.postgre;

import com.saurav.SpringAzureOpenAI.dao.PGDocument;
import com.saurav.SpringAzureOpenAI.dao.PGEngineer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PGController {

    @Autowired
    private PGService pgService;

    @PostMapping("/uploadDocument")
    public String uploadDocToPostgreSQL(@RequestBody List<PGDocument> documents) {
        pgService.uploadDocumentDataToPostgreSQL(documents);
        return "Data uploaded to PostgreSQL successfully!";
    }

    @PostMapping("/retriveDocsByQuery")
    public List<PGDocumentDTO> retrieveDocuments(@RequestBody String  query) {
        return pgService.retrieveDocument(query);
    }

    @PostMapping("/uploadEngineer")
    public String uploadEngToPostgreSQL(@RequestBody List<PGEngineer> engineers) {
        pgService.uploadEngineerDataToPostgreSQL(engineers);
        return "Data uploaded to PostgreSQL successfully!";
    }

    @PostMapping("/retriveEngsByQuery")
    public List<PGEngineerDTO> retrieveEngineers(@RequestBody String  query) {
        return pgService.retrieveEngineers(query);
    }
}
