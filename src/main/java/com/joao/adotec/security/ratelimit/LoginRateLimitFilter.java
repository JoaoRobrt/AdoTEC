package com.joao.adotec.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Servlet filter that enforces rate limiting on the login endpoint ({@code POST /auth/login}).
 * <p>
 * For each incoming login request, a distributed token-bucket is resolved from Redis
 * using the client's IP address as the key. If the bucket is exhausted, the filter
 * short-circuits the request with an HTTP 429 response.
 * </p>
 *
 * <h3>Client identification strategy</h3>
 * The client IP is resolved by checking the {@code X-Forwarded-For} header first
 * (for requests behind a reverse proxy), falling back to {@code request.getRemoteAddr()}.
 *
 * <h3>Response headers</h3>
 * <ul>
 *     <li>{@code X-Rate-Limit-Remaining} — tokens left in the bucket</li>
 *     <li>{@code X-Rate-Limit-Retry-After-Seconds} — seconds until the bucket refills (only on 429)</li>
 *     <li>{@code Retry-After} — standard HTTP header (only on 429)</li>
 * </ul>
 *
 * @see RateLimitConfig
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final String LOGIN_PATH = "/auth/login";
    private static final String RATE_LIMIT_KEY_PREFIX = "rate-limit:login:";

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfigSupplier;
    private final ObjectMapper objectMapper;

    public LoginRateLimitFilter(ProxyManager<String> proxyManager,
                                 RateLimitProperties props,
                                 ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.objectMapper = objectMapper;
        this.bucketConfigSupplier = buildConfigSupplier(props);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply rate limiting to POST /auth/login
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String bucketKey = RATE_LIMIT_KEY_PREFIX + clientIp;

        ConsumptionProbe probe = proxyManager.builder()
                .build(bucketKey, bucketConfigSupplier)
                .tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            if (retryAfterSeconds == 0) {
                retryAfterSeconds = 1; // at minimum 1 second
            }

            logger.warn("Rate limit exceeded for IP [{}] on login endpoint. Retry after {}s",
                    clientIp, retryAfterSeconds);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSeconds));
            response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));

            // RFC 7807-style problem detail response
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", "about:blank");
            body.put("title", "Too Many Requests");
            body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            body.put("detail", "Muitas tentativas de login. Tente novamente em " + retryAfterSeconds + " segundos.");
            body.put("instance", request.getRequestURI());
            body.put("timestamp", Instant.now().toString());

            objectMapper.writeValue(response.getOutputStream(), body);
        }
    }

    /**
     * Checks if the request targets the login endpoint.
     */
    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());
    }

    /**
     * Resolves the real client IP, considering reverse proxy headers.
     * <p>
     * Priority: {@code X-Forwarded-For} (first IP) → {@code request.getRemoteAddr()}.
     * </p>
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For may contain: "client, proxy1, proxy2"
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Builds the bucket configuration supplier with the defined rate limit policy.
     */
    private Supplier<BucketConfiguration> buildConfigSupplier(RateLimitProperties props) {
        return () -> BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(props.getCapacity())
                                .refillGreedy(props.getRefillTokens(),
                                        Duration.ofSeconds(props.getRefillDuration()))
                                .build()
                )
                .build();
    }
}
