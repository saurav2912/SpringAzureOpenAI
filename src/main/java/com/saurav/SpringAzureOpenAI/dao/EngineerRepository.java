package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineerRepository extends CosmosRepository<Engineer, String> {
   /* @Query(value = "SELECT * FROM c ORDER BY VectorDistance(c.embedding, @embedding) ASC OFFSET 0 LIMIT 5",
    nativeQuery = true)
    List<Engineer> findTop5ByOrderByVectorDistance(@Param("embedding")float[] embedding);*/
}
