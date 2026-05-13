package com.joao.adotec.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configures the distributed rate-limiting infrastructure backed by Redis.
 * <p>
 * This configuration is only activated when {@code rate-limit.login.enabled=true}.
 * It creates a {@link ProxyManager} that stores token-bucket state in Redis,
 * enabling rate limiting to work correctly across multiple application instances.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "rate-limit.login.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitConfig {

    /**
     * Creates a Lettuce-based {@link ProxyManager} that persists bucket state in Redis.
     */
    @Bean
    public ProxyManager<String> lettuceProxyManager(
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${spring.data.redis.password:}") String redisPassword,
            RateLimitProperties rateLimitProps) {

        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort);

        if (redisPassword != null && !redisPassword.isBlank()) {
            uriBuilder.withPassword(redisPassword.toCharArray());
        }

        RedisClient redisClient = RedisClient.create(uriBuilder.build());

        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        // TTL = refill period + 30s buffer, so idle buckets auto-expire from Redis
        Duration bucketTtl = Duration.ofSeconds(rateLimitProps.getRefillDuration() + 30L);

        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(bucketTtl)
                )
                .build();
    }

    /**
     * Creates the login rate limit filter bean, injecting the ProxyManager
     * created above. This guarantees correct bean ordering.
     */
    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(ProxyManager<String> lettuceProxyManager,
                                                      RateLimitProperties rateLimitProps) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new LoginRateLimitFilter(lettuceProxyManager, rateLimitProps, objectMapper);
    }
}
