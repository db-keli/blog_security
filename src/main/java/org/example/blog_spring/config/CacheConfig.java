package org.example.blog_spring.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeine() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        var cacheManager = new CaffeineCacheManager(
                "posts",
                "postsBySlug",
                "postLists",
                "tags",
                "tagsBySlug",
                "tagLists",
                "users",
                "userLists"
        );
        cacheManager.setCaffeine(caffeine);
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}

