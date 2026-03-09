package org.example.blog_spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

class JwtTokenProviderTest {

    @Test
    void createToken_containsSubjectRolesAndExpiry() {
        JwtTokenProvider provider = new JwtTokenProvider(
                "test-secret-test-secret-test-secret-test-secret",
                60_000L);

        var userDetails = new User("jdoe@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_AUTHOR")));

        String token = provider.createToken(userDetails);

        assertThat(token).isNotBlank();

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("jdoe@example.com");
        assertThat(provider.getRoles(token)).contains("ROLE_AUTHOR");
    }
}

