package org.example.blog_spring.domain;

import java.time.Instant;
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
public class Comment {

    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public Comment(Long postId, Long userId, String content, Long parentId) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
