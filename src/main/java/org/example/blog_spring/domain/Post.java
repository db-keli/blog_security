package org.example.blog_spring.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts",
                indexes = {@Index(name = "idx_posts_author", columnList = "author_id"),
                                @Index(name = "idx_posts_title", columnList = "title"),
                                @Index(name = "idx_posts_slug", columnList = "slug"),
                                @Index(name = "idx_posts_status", columnList = "status"),
                                @Index(name = "idx_posts_created", columnList = "created_at"),
                                @Index(name = "idx_posts_published", columnList = "published_at")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "author_id", nullable = false,
                        foreignKey = @ForeignKey(name = "fk_posts_author"))
        private User author;

        @Column(nullable = false, length = 255)
        @Setter
        private String title;

        @Column(nullable = false, columnDefinition = "text")
        @Setter
        private String content;

        @Column(nullable = false, length = 255, unique = true)
        @Setter
        private String slug;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @Setter
        private PostStatus status = PostStatus.DRAFT;

        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @Column(name = "published_at")
        @Setter
        private Instant publishedAt;

        @ManyToMany
        @JoinTable(name = "post_tags",
                        joinColumns = @JoinColumn(name = "post_id",
                                        foreignKey = @ForeignKey(name = "fk_post_tags_post")),
                        inverseJoinColumns = @JoinColumn(name = "tag_id",
                                        foreignKey = @ForeignKey(name = "fk_post_tags_tag")))
        @Setter
        private Set<Tag> tags = new HashSet<>();

        public Post(User author, String title, String content, String slug) {
                this.author = author;
                this.title = title;
                this.content = content;
                this.slug = slug;
                this.status = PostStatus.DRAFT;
                var now = Instant.now();
                this.createdAt = now;
                this.updatedAt = now;
        }
}

