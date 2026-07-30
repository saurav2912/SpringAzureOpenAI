package com.saurav.SpringAzureOpenAI.dao;


import com.pgvector.PGvector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name="documents")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PGDocument {

    @Id
    private String id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String category;
    //@Column(columnDefinition = "vector(1536)")
    //private PGvector embedding;
    //@Type(Pgvec)
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    // getters/setters
}
