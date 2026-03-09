package org.example.blog_spring.security;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofHours(1);

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        revokedTokens.put(token, Instant.now());
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Instant revokedAt = revokedTokens.get(token);
        if (revokedAt == null) {
            return false;
        }
        if (revokedAt.plus(ttl).isBefore(Instant.now())) {
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }
}

