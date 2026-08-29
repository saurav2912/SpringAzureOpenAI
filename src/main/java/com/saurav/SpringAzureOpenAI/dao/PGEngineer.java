package com.saurav.SpringAzureOpenAI.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Employee")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PGEngineer {
    @Id
    private String id;
    private String name;
    private String role;
    @Column(columnDefinition = "TEXT")
    private String profile;
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
}
