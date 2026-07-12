package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.util.List;

@Container(containerName = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    private String id;
    private String title;
    private String content;
    private String category;
    private List<Float> vector;
}