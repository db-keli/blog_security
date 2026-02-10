package org.example.blog_spring.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new CacheManager();
    }

    @Test
    void putPostAndGetPost_returnsCachedPost() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();

        cacheManager.putPost(post);
        var result = cacheManager.getPost(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test");
    }

    @Test
    void putPost_alsoCachesBySlug() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("my-slug").tags(Set.of())
                .build();

        cacheManager.putPost(post);
        var bySlug = cacheManager.getPostBySlug("my-slug");

        assertThat(bySlug).isNotNull();
        assertThat(bySlug.getId()).isEqualTo(1L);
    }

    @Test
    void getPost_returnsNull_whenNotCached() {
        var result = cacheManager.getPost(999L);

        assertThat(result).isNull();
    }

    @Test
    void removePost_removesFromCache() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        cacheManager.putPost(post);

        cacheManager.removePost(1L);

        assertThat(cacheManager.getPost(1L)).isNull();
    }

    @Test
    void putTagAndGetTag_returnsCachedTag() {
        var tag = Tag.builder().id(1L).name("Java").slug("java").build();

        cacheManager.putTag(tag);
        var result = cacheManager.getTag(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Java");
    }

    @Test
    void getTagBySlug_returnsCachedTag() {
        var tag = Tag.builder().id(1L).name("Java").slug("java").build();
        cacheManager.putTag(tag);

        var result = cacheManager.getTagBySlug("java");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void putPostListAndGetPostList_returnsCachedPage() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        var entry = new CacheManager.PageCacheEntry(List.of(post), 1L);
        var key = CacheManager.buildPostListKey(null, null, null, null, 0, 20);

        cacheManager.putPostList(key, entry);
        var result = cacheManager.getPostList(key);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    void buildPostListKey_includesAllParams() {
        var key = CacheManager.buildPostListKey(1L, "java", "search", PostStatus.PUBLISHED, 2, 10);

        assertThat(key).contains("1");
        assertThat(key).contains("java");
        assertThat(key).contains("search");
        assertThat(key).contains("PUBLISHED");
        assertThat(key).contains("2");
        assertThat(key).contains("10");
    }

    @Test
    void getStatistics_returnsHitAndMissCounts() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        cacheManager.putPost(post);

        cacheManager.getPost(1L); // hit
        cacheManager.getPost(1L); // hit
        cacheManager.getPost(999L); // miss

        var stats = cacheManager.getStatistics();

        assertThat(stats.postCacheHits).isEqualTo(2);
        assertThat(stats.postCacheMisses).isEqualTo(1);
        assertThat(stats.getHitRate()).isGreaterThan(0.5);
    }

    @Test
    void clearAll_removesAllEntries() {
        var post = Post.builder().id(1L).authorId(1L).title("Test").slug("test").tags(Set.of())
                .build();
        var tag = Tag.builder().id(1L).name("Java").slug("java").build();
        cacheManager.putPost(post);
        cacheManager.putTag(tag);

        cacheManager.clearAll();

        assertThat(cacheManager.getPost(1L)).isNull();
        assertThat(cacheManager.getTag(1L)).isNull();
    }
}
