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
public class Tag {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Instant createdAt;

    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.createdAt = Instant.now();
    }
}
