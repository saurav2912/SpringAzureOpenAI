package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.*;
import org.springframework.data.annotation.Id;

@Container(containerName = "Employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    private String id;
    private String name;
    private String department;
    private double salary;
}
