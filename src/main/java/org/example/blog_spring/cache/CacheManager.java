package org.example.blog_spring.cache;

import org.example.blog_spring.domain.Post;
import org.example.blog_spring.domain.PostStatus;
import org.example.blog_spring.domain.Tag;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache manager using LRU (Least Recently Used) eviction policy.
 * Provides in-memory caching for frequently accessed data.
 */
@Component
public class CacheManager {

    // LRU cache for posts - using LinkedHashMap with access order
    private final Map<Long, Post> postCache;
    private final int maxPostCacheSize;

    // Cache for posts by slug
    private final Map<String, Post> postSlugCache;
    private final int maxPostSlugCacheSize;

    // Cache for tags - smaller and frequently accessed
    private final Map<Long, Tag> tagCache;
    private final Map<String, Tag> tagSlugCache;
    private final int maxTagCacheSize;

    // Cache for post lists (search results with pagination)
    private final Map<String, PageCacheEntry> postListCache;
    private final int maxPostListCacheSize;

    // Statistics
    private long postCacheHits = 0;
    private long postCacheMisses = 0;

    public CacheManager() {
        this.maxPostCacheSize = 100;
        this.maxPostSlugCacheSize = 100;
        this.maxTagCacheSize = 50;
        this.maxPostListCacheSize = 20;

        this.postCache = Collections.synchronizedMap(
                new LinkedHashMap<Long, Post>(maxPostCacheSize, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Long, Post> eldest) {
                        return size() > maxPostCacheSize;
                    }
                }
        );

        this.postSlugCache = Collections.synchronizedMap(
                new LinkedHashMap<String, Post>(maxPostSlugCacheSize, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Post> eldest) {
                        return size() > maxPostSlugCacheSize;
                    }
                }
        );

        this.tagCache = Collections.synchronizedMap(
                new LinkedHashMap<Long, Tag>(maxTagCacheSize, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Long, Tag> eldest) {
                        return size() > maxTagCacheSize;
                    }
                }
        );

        this.tagSlugCache = Collections.synchronizedMap(
                new LinkedHashMap<String, Tag>(maxTagCacheSize, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Tag> eldest) {
                        return size() > maxTagCacheSize;
                    }
                }
        );

        this.postListCache = new ConcurrentHashMap<>();
    }

    /**
     * Gets a post from cache by id.
     */
    public Post getPost(Long postId) {
        Post post = postCache.get(postId);
        if (post != null) {
            postCacheHits++;
        } else {
            postCacheMisses++;
        }
        return post;
    }

    /**
     * Puts a post in cache.
     */
    public void putPost(Post post) {
        if (post != null && post.getId() != null) {
            postCache.put(post.getId(), post);
            if (post.getSlug() != null) {
                postSlugCache.put(post.getSlug(), post);
            }
        }
    }

    /**
     * Removes a post from cache.
     */
    public void removePost(Long postId) {
        Post removed = postCache.remove(postId);
        if (removed != null && removed.getSlug() != null) {
            postSlugCache.remove(removed.getSlug());
        } else if (postId != null) {
            String slugToRemove = null;
            for (var e : postSlugCache.entrySet()) {
                if (postId.equals(e.getValue().getId())) {
                    slugToRemove = e.getKey();
                    break;
                }
            }
            if (slugToRemove != null) {
                postSlugCache.remove(slugToRemove);
            }
        }
        invalidatePostListCache();
    }

    /**
     * Gets a post from cache by slug.
     */
    public Post getPostBySlug(String slug) {
        Post post = postSlugCache.get(slug);
        if (post != null) {
            postCacheHits++;
        } else {
            postCacheMisses++;
        }
        return post;
    }

    /**
     * Gets a tag from cache by id.
     */
    public Tag getTag(Long tagId) {
        return tagCache.get(tagId);
    }

    /**
     * Gets a tag from cache by slug.
     */
    public Tag getTagBySlug(String slug) {
        return tagSlugCache.get(slug);
    }

    /**
     * Puts a tag in cache.
     */
    public void putTag(Tag tag) {
        if (tag != null && tag.getId() != null) {
            tagCache.put(tag.getId(), tag);
            if (tag.getSlug() != null) {
                tagSlugCache.put(tag.getSlug(), tag);
            }
        }
    }

    /**
     * Removes a tag from cache.
     */
    public void removeTag(Long tagId) {
        Tag removed = tagCache.remove(tagId);
        if (removed != null && removed.getSlug() != null) {
            tagSlugCache.remove(removed.getSlug());
        } else if (tagId != null) {
            String slugToRemove = null;
            for (var e : tagSlugCache.entrySet()) {
                if (tagId.equals(e.getValue().getId())) {
                    slugToRemove = e.getKey();
                    break;
                }
            }
            if (slugToRemove != null) {
                tagSlugCache.remove(slugToRemove);
            }
        }
        invalidatePostListCache();
    }

    /**
     * Gets a cached post list page.
     */
    public PageCacheEntry getPostList(String key) {
        return postListCache.get(key);
    }

    /**
     * Puts a post list page in cache.
     */
    public void putPostList(String key, PageCacheEntry entry) {
        if (entry != null) {
            if (postListCache.size() >= maxPostListCacheSize) {
                postListCache.keySet().stream().findFirst().ifPresent(postListCache::remove);
            }
            postListCache.put(key, entry);
        }
    }

    /**
     * Builds a cache key for post list queries.
     */
    public static String buildPostListKey(Long authorId, String tagSlug, String search,
            PostStatus status, int page, int size) {
        return "posts:" + (authorId != null ? authorId : "all") + ":"
                + (tagSlug != null ? tagSlug : "all") + ":"
                + (search != null ? search : "all") + ":"
                + (status != null ? status.name() : "all") + ":"
                + page + ":" + size;
    }

    /**
     * Clears all posts from cache.
     */
    public void clearPostCache() {
        postCache.clear();
        postSlugCache.clear();
        invalidatePostListCache();
    }

    /**
     * Clears all tags from cache.
     */
    public void clearTagCache() {
        tagCache.clear();
        tagSlugCache.clear();
    }

    /**
     * Invalidates all post list caches.
     */
    public void invalidatePostListCache() {
        postListCache.clear();
    }

    /**
     * Clears all caches.
     */
    public void clearAll() {
        clearPostCache();
        clearTagCache();
    }

    /**
     * Gets cache statistics.
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(
                postCache.size(),
                postSlugCache.size(),
                tagCache.size(),
                postListCache.size(),
                postCacheHits,
                postCacheMisses
        );
    }

    /**
     * Holder for a cached page of posts.
     */
    public record PageCacheEntry(List<Post> content, long totalElements) {
    }

    /**
     * Cache statistics holder.
     */
    public static class CacheStatistics {
        public final int postCacheSize;
        public final int postSlugCacheSize;
        public final int tagCacheSize;
        public final int postListCacheSize;
        public final long postCacheHits;
        public final long postCacheMisses;

        public CacheStatistics(int postCacheSize, int postSlugCacheSize, int tagCacheSize,
                int postListCacheSize, long postCacheHits, long postCacheMisses) {
            this.postCacheSize = postCacheSize;
            this.postSlugCacheSize = postSlugCacheSize;
            this.tagCacheSize = tagCacheSize;
            this.postListCacheSize = postListCacheSize;
            this.postCacheHits = postCacheHits;
            this.postCacheMisses = postCacheMisses;
        }

        public double getHitRate() {
            long total = postCacheHits + postCacheMisses;
            return total > 0 ? (double) postCacheHits / total : 0.0;
        }
    }
}
