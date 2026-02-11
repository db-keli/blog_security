package org.example.blog_spring.web.rest;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        var stats = cacheManager.getStatistics();
        var data = Map.<String, Object>of(
                "postCacheSize", stats.postCacheSize,
                "postSlugCacheSize", stats.postSlugCacheSize,
                "tagCacheSize", stats.tagCacheSize,
                "postListCacheSize", stats.postListCacheSize,
                "postCacheHits", stats.postCacheHits,
                "postCacheMisses", stats.postCacheMisses,
                "hitRate", stats.getHitRate()
        );
        var response = ApiResponse.success(HttpStatus.OK, "Cache statistics", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        cacheManager.clearAll();
        var response = ApiResponse.<Void>success(HttpStatus.OK, "Cache cleared", null);
        return ResponseEntity.ok(response);
    }
}
