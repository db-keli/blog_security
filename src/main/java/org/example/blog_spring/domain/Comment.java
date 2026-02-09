package org.example.blog_spring.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments",
                indexes = {@Index(name = "idx_comments_post", columnList = "post_id"),
                                @Index(name = "idx_comments_user", columnList = "user_id"),
                                @Index(name = "idx_comments_parent", columnList = "parent_id"),
                                @Index(name = "idx_comments_created", columnList = "created_at")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "post_id", nullable = false,
                        foreignKey = @ForeignKey(name = "fk_comments_post"))
        private Post post;

        @ManyToOne(optional = false)
        @JoinColumn(name = "user_id", nullable = false,
                        foreignKey = @ForeignKey(name = "fk_comments_user"))
        private User user;

        @ManyToOne
        @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comments_parent"))
        private Comment parent;

        @Column(nullable = false, columnDefinition = "text")
        @Setter
        private String content;

        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public Comment(Post post, User user, String content, Comment parent) {
                this.post = post;
                this.user = user;
                this.content = content;
                this.parent = parent;
                var now = Instant.now();
                this.createdAt = now;
                this.updatedAt = now;
        }
}

