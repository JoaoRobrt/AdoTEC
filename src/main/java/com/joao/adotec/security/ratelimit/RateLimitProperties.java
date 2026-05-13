package com.joao.adotec.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration properties for the login rate limiting mechanism.
 * <p>
 * Mapped from the {@code rate-limit.login} namespace in application YAML files.
 * </p>
 *
 * <pre>
 * rate-limit:
 *   login:
 *     enabled: true
 *     capacity: 5
 *     refill-tokens: 5
 *     refill-duration: 60
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit.login")
public class RateLimitProperties {

    /**
     * Whether the login rate limiter is active.
     * Set to false to disable rate limiting (useful for tests).
     */
    private boolean enabled = true;

    /**
     * Maximum number of tokens (attempts) in the bucket.
     */
    private int capacity = 5;

    /**
     * Number of tokens refilled per period.
     */
    private int refillTokens = 5;

    /**
     * Refill period duration in seconds.
     */
    private int refillDuration = 60;
}
