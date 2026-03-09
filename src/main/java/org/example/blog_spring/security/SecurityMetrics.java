package org.example.blog_spring.security;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class SecurityMetrics {

    private final AtomicLong loginSuccess = new AtomicLong();
    private final AtomicLong loginFailure = new AtomicLong();
    private final AtomicLong tokenValidated = new AtomicLong();
    private final AtomicLong tokenRejected = new AtomicLong();

    public void incrementLoginSuccess() {
        loginSuccess.incrementAndGet();
    }

    public void incrementLoginFailure() {
        loginFailure.incrementAndGet();
    }

    public void incrementTokenValidated() {
        tokenValidated.incrementAndGet();
    }

    public void incrementTokenRejected() {
        tokenRejected.incrementAndGet();
    }

    public Map<String, Long> snapshot() {
        return Map.of(
                "loginSuccess", loginSuccess.get(),
                "loginFailure", loginFailure.get(),
                "tokenValidated", tokenValidated.get(),
                "tokenRejected", tokenRejected.get());
    }
}

