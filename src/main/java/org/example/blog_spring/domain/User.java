package org.example.blog_spring.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Setter
    private String username;

    @Column(nullable = false, length = 255)
    @Setter
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @Setter
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    @Setter
    private String displayName;

    @Column(columnDefinition = "text")
    @Setter
    private String bio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public User(String username, String email, String displayName) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        // TODO set a proper password hash during user registration
        this.passwordHash = "";
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}

