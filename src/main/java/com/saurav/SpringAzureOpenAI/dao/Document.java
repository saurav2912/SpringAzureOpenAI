package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;

@Container(containerName = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Document {

    @Id
    private String id;
    private String title;
    private String content;
    private String category;
    private List<Float> vector;
}