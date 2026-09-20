package com.saurav.SpringAzureOpenAI.Redis;

import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.authentication.core.TokenAuthConfig;
import redis.clients.authentication.entraid.AzureTokenAuthConfigBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.authentication.AuthXManager;
import redis.clients.jedis.search.*;

import java.util.Map;
import java.util.Set;

@Configuration
public class RedisConfig {

    private static final String REDIS_SCOPE =
            "https://redis.azure.com/.default";

    @Bean
    public UnifiedJedis jedis() {

        HostAndPort host = new HostAndPort(
                "ampazai-redis.centralindia.redis.azure.net",
                10000);

        TokenAuthConfig tokenAuthConfig =
                AzureTokenAuthConfigBuilder.builder()
                        .defaultAzureCredential(
                                new DefaultAzureCredentialBuilder().build())
                        .scopes(Set.of(REDIS_SCOPE))
                        .tokenRequestExecTimeoutInMs(20000)
                        .build();

        DefaultJedisClientConfig config =
                DefaultJedisClientConfig.builder()
                        .authXManager(new AuthXManager(tokenAuthConfig))
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
