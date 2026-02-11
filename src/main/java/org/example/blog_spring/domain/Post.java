package org.example.blog_spring.domain;

import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private String slug;
    private PostStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
    private Set<Tag> tags;

    public Post(Long authorId, String title, String content, String slug) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.slug = slug;
        this.status = PostStatus.DRAFT;
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
