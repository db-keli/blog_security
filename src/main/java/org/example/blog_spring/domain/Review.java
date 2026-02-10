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
public class Review {

    private Long id;
    private Long postId;
    private Long userId;
    private short rating;
    private String title;
    private String content;
    private boolean verified;
    private Instant createdAt;
    private Instant updatedAt;

    public Review(Long postId, Long userId, short rating) {
        this.postId = postId;
        this.userId = userId;
        this.rating = rating;
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
