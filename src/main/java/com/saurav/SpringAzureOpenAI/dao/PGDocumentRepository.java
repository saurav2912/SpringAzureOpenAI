package com.saurav.SpringAzureOpenAI.dao;

import com.saurav.SpringAzureOpenAI.postgre.PGDocumentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PGDocumentRepository extends JpaRepository<PGDocument, String> {

    @Query(value = "SELECT id,title,content,category FROM documents ORDER BY embedding <=>  CAST(:embedding AS vector) LIMIT 5",
            nativeQuery = true)
    List<PGDocumentDTO> findByContentContaining(@Param("embedding")float[] queryEmbedding);
}
