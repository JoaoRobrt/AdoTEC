package com.joao.adotec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Configuração centralizada de cache Redis.
 * Reutiliza a mesma conexão Redis já usada pelo Bucket4j (rate limiting).
 *
 * Usa {@link GenericJacksonJsonRedisSerializer} (Jackson 3.x) — substituto do
 * depreciado {@code GenericJackson2JsonRedisSerializer} removido no Spring Data
 * Redis 4.0.
 *
 * TTLs configuráveis via application.yml / application-dev.yml:
 *   cache.pets.destaque.ttl-minutes=10
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.pets.destaque.ttl-minutes:10}")
    private long petsDestaqueTtlMinutes;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Jackson 3.x (tools.jackson) já possui suporte nativo a java.time.*,
        // dispensando o módulo JSR-310 que era necessário no Jackson 2.
        // Jackson 3 serializa java.time.* como ISO-8601 por padrão (sem necessidade de configuração extra).
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(
                        tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build())
                .build();

        // Serialização padrão: chave String, valor JSON com suporte a Instant
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Configuração específica do cache "petsDestaque" com TTL dedicado
        RedisCacheConfiguration petsDestaqueConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(petsDestaqueTtlMinutes));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        "petsDestaque", petsDestaqueConfig
                ))
                .build();
    }
}
