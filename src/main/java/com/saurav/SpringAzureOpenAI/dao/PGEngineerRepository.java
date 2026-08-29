package com.saurav.SpringAzureOpenAI.dao;

import com.saurav.SpringAzureOpenAI.postgre.PGEngineerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PGEngineerRepository extends JpaRepository<PGEngineer,String> {

    @Query(value = "SELECT id,name,role,profile FROM employee ORDER BY embedding <=>  CAST(:embedding AS vector) LIMIT 5",
            nativeQuery = true)
    List<PGEngineerDTO> findByContentContaining(@Param("embedding")float[] queryEmbedding);

}
