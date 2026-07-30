package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "Engineer")
public class Engineer {
    private String id;
    private String name;
    private String role;
    private String profile;
    private float[] embedding;

}
