package org.example.blog_spring.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.example.blog_spring.cache.CacheManager;
import org.example.blog_spring.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CacheControllerTest {

    private final CacheManager cacheManager = Mockito.mock(CacheManager.class);
    private final CacheController controller = new CacheController(cacheManager);

    @Test
    void getStatistics_returnsCacheStats() {
        var stats = new CacheManager.CacheStatistics(1, 1, 2, 0, 5, 5);
        given(cacheManager.getStatistics()).willReturn(stats);

        var response = controller.getStatistics();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cache statistics");
        assertThat(response.getBody().data()).containsKey("postCacheSize");
        assertThat(response.getBody().data().get("postCacheSize")).isEqualTo(1);
    }

    @Test
    void clearCache_returnsOk() {
        var response = controller.clearCache();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cache cleared");
    }
}
