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
public class User {

    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String bio;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String username, String email, String displayName) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = "";
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
