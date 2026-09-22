package com.saurav.SpringAzureOpenAI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AI200Controller {

    private final List<byte[]> memoryStore = new ArrayList<>();

    @GetMapping("/ai200")
    public ResponseEntity<String> getAI200() {

        return ResponseEntity.ok("AI200 is running in Azure Container Apps with Redis on 22 Sep 8.30 AM!!!");
    }

    @GetMapping("/cpu")
    public String cpu() {
        long result = 0;

        for (long i = 0; i < 2_000_000_000L; i++) {
            result += Math.sqrt(i);
        }

        return "done " + result;
    }



    @GetMapping("/memory")
    public String memory() {

        for (int i = 0; i < 100; i++) {
            // Allocate 1 MB each iteration
            memoryStore.add(new byte[1024 * 1024]);
        }

        return "Allocated approximately 100 MB";
    }
}
