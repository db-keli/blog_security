package org.example.blog_spring.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CacheControllerTest {

    private final CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private final CacheController controller = new CacheController(cacheManager);

    @Test
    void getStatistics_returnsCacheStats() {
        var nativeCache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .recordStats()
                .build();
        nativeCache.put("k", "v");
        nativeCache.getIfPresent("k"); // hit
        nativeCache.getIfPresent("missing"); // miss

        var caffeineCache = new CaffeineCache("posts", nativeCache, false);
        given(cacheManager.getCacheNames()).willReturn(java.util.List.of("posts"));
        given(cacheManager.getCache("posts")).willReturn(caffeineCache);

        var response = controller.getStatistics();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cache statistics");
        assertThat(response.getBody().data()).containsKey("caches");
        @SuppressWarnings("unchecked")
        var caches = (java.util.Map<String, Object>) response.getBody().data().get("caches");
        assertThat(caches).containsKey("posts");
        @SuppressWarnings("unchecked")
        var postsCache = (java.util.Map<String, Object>) caches.get("posts");
        assertThat(postsCache.get("estimatedSize")).isEqualTo(1L);
        assertThat(postsCache.get("hitCount")).isEqualTo(1L);
        assertThat(postsCache.get("missCount")).isEqualTo(1L);
    }

    @Test
    void clearCache_returnsOk() {
        Cache springCache = Mockito.mock(Cache.class);
        given(cacheManager.getCacheNames()).willReturn(java.util.List.of("posts"));
        given(cacheManager.getCache("posts")).willReturn(springCache);

        var response = controller.clearCache();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cache cleared");
        verify(springCache).clear();
    }
}
