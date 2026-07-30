package com.saurav.SpringAzureOpenAI.cosmos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EngineerContoller {

    @Autowired
    private EngineerService engineerService;

    @PostMapping("/insertEngineer")
    public String insertEngineer(@RequestBody List<EngineerDTO> engineerDTOList) {
        engineerService.createEnginners(engineerDTOList);
        return "Engineer data inserted successfully!";
    }

    @GetMapping("/queryRAG")
    public String queryRAG(@RequestHeader String query) {
        String result = engineerService.performRAGQuery(query);
        return result;
    }
}
