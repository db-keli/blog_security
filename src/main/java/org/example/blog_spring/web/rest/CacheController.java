package org.example.blog_spring.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.blog_spring.dto.ApiResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@Tag(name = "Cache", description = "Cache management APIs (Spring Cache / Caffeine)")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get cache statistics from Spring Cache (Caffeine)")
    @ApiResponses(
            value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Cache statistics retrieved successfully")})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Collection<String> cacheNames = cacheManager.getCacheNames();

        var caches = new LinkedHashMap<String, Object>();
        long totalEstimatedSize = 0L;

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                continue;
            }

            var cacheData = new LinkedHashMap<String, Object>();
            cacheData.put("springCacheType", cache.getClass().getName());

            if (cache instanceof CaffeineCache caffeineCache) {
                var nativeCache = caffeineCache.getNativeCache();
                var stats = nativeCache.stats();
                long estimatedSize = nativeCache.estimatedSize();

                cacheData.put("estimatedSize", estimatedSize);
                cacheData.put("requestCount", stats.requestCount());
                cacheData.put("hitCount", stats.hitCount());
                cacheData.put("missCount", stats.missCount());
                cacheData.put("hitRate", stats.hitRate());
                cacheData.put("missRate", stats.missRate());
                cacheData.put("loadSuccessCount", stats.loadSuccessCount());
                cacheData.put("loadFailureCount", stats.loadFailureCount());
                cacheData.put("totalLoadTimeNanos", stats.totalLoadTime());
                cacheData.put("averageLoadPenaltyNanos", stats.averageLoadPenalty());
                cacheData.put("evictionCount", stats.evictionCount());
                cacheData.put("evictionWeight", stats.evictionWeight());

                totalEstimatedSize += estimatedSize;
            } else {
                cacheData.put("note", "Cache is not backed by Caffeine; stats unavailable");
            }

            caches.put(cacheName, cacheData);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cacheProvider", cacheManager.getClass().getName());
        data.put("cacheNames", List.copyOf(cacheNames));
        data.put("totalEstimatedSize", totalEstimatedSize);
        data.put("caches", caches);

        var response = ApiResponse.success(HttpStatus.OK, "Cache statistics", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    @Operation(summary = "Clear all caches in Spring Cache")
    @ApiResponses(
            value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Cache cleared successfully")})
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        var response = ApiResponse.<Void>success(HttpStatus.OK, "Cache cleared", null);
        return ResponseEntity.ok(response);
    }
}
