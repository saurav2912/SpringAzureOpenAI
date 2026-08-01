package com.saurav.SpringAzureOpenAI.Redis;

import com.saurav.SpringAzureOpenAI.dao.PGDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RedisEngService {

    @Autowired
    private UnifiedJedis jedis;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatClient chatClient;

    public void insertEngineers(List<RedisEngDTO> engDTOList) {
        int batchSize = 200;
        for (int i = 0; i < engDTOList.size(); i += batchSize) {

            List<RedisEngDTO> batch = engDTOList.subList(
                    i,
                    Math.min(i + batchSize, engDTOList.size())
            );

            List<String> texts = batch.stream()
                    .map(RedisEngDTO::profile)
                    .toList();

            List<float[]> embeddings = embeddingModel.embed(texts);

            for (int j = 0; j < batch.size(); j++) {
                byte[] vector = toByteArray(embeddings.get(j));
                Map<byte[], byte[]> map = new HashMap<>();

                map.put("title".getBytes(StandardCharsets.UTF_8),
                        batch.get(j).name().getBytes(StandardCharsets.UTF_8));

                map.put("category".getBytes(StandardCharsets.UTF_8),
                        batch.get(j).role().getBytes(StandardCharsets.UTF_8));

                map.put("content".getBytes(StandardCharsets.UTF_8),
                        batch.get(j).profile().getBytes(StandardCharsets.UTF_8));

                map.put("embedding".getBytes(StandardCharsets.UTF_8),
                        vector);

                jedis.hset(
                        ("doc:" + batch.get(j).id()).getBytes(StandardCharsets.UTF_8),
                        map);
            }


        }
    }

    private byte[] toByteArray(float[] vector){

        ByteBuffer buffer =
                ByteBuffer.allocate(vector.length*Float.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN);

        for(float f:vector){

            buffer.putFloat(f);

        }

        return buffer.array();

    }


    private List<Map<String,Object>> searchDocument(String question) {

        float[] embedding =
                embeddingModel.embed(question);

        byte[] vector = toByteArray(embedding);

        String query =
                "*=>[KNN 50 @embedding $vec AS score]";

        Query q = new Query(query)
                .addParam("vec", vector)
                .returnFields(
                        "title",
                        "content",
                        "category",
                        "score")
                .setSortBy("score", true)
                .dialect(2);

        SearchResult result =
                jedis.ftSearch("engineering_idx", q);

        List<Map<String,Object>> documents =
                new ArrayList<>();

        for (Document doc : result.getDocuments()) {

            Map<String,Object> map = new HashMap<>();

            map.put("id", doc.getId());
            map.put("title", doc.getString("title"));
            map.put("content", doc.getString("content"));
            map.put("category", doc.getString("category"));
            map.put("score", doc.getString("score"));

            documents.add(map);
        }

        return documents;
    }

    public String getResultfromQuery(String query){

        List<Map<String,Object>> documents = searchDocument(query);
        StringBuilder context = new StringBuilder();
        for(Map<String,Object> doc:documents){
            context.append(doc.get("content")).append("\n");
        }
        String prompt = "Answer the question based on the context below:\n\nContext:\n" + context + "\nQuestion: " + query;
        String answer = chatClient.prompt(prompt).advisors
                (x->x.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID())).call().content();

        return answer;
    }
}
