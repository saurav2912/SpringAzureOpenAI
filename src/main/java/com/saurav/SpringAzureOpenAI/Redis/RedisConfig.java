package com.saurav.SpringAzureOpenAI.Redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.*;

import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public UnifiedJedis jedis() {

        HostAndPort host = new HostAndPort(
                "redis-smp-az.centralindia.redis.azure.net",
                10000);

        DefaultJedisClientConfig config =
                DefaultJedisClientConfig.builder()
                        .password("emAMMA_fVDNLe2qlWqlyHSi9d6OkJcmnQAZCAACgJ2o=")
                        .ssl(true)
                        .build();
        UnifiedJedis jedis = new UnifiedJedis(host, config);
        //jedis.flushAll();
        //createIndex(jedis);
        return jedis;
    }

    public void createIndex(UnifiedJedis jedis) {
        IndexDefinition def = new IndexDefinition().setPrefixes(new String[]{"doc:"});
        jedis.ftCreate(
                "engineering_idx",
                IndexOptions.defaultOptions().setDefinition(def),
                new Schema()

                        .addTextField("title", 1)

                        .addTextField("category", 1)

                        .addTextField("content", 1)

                        .addVectorField(
                                "embedding",
                                Schema.VectorField.VectorAlgo.HNSW,
                                Map.of(
                                        "TYPE", "FLOAT32",
                                        "DIM", 1536,
                                        "DISTANCE_METRIC", "COSINE",
                                        "M", 16,
                                        "EF_CONSTRUCTION", 200
                                )));
        System.out.println("Index created successfully.");
    }
}
