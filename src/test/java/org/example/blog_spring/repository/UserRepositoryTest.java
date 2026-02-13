package org.example.blog_spring.repository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.blog_spring.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void saveAndFindById_roundTrip() {
        var id = unique();
        var user = User.builder().username("jdoe-" + id).email("jdoe-" + id + "@example.com")
                .displayName("John Doe").passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        var saved = userRepository.save(user);

        var found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("jdoe-" + id);
    }

    @Test
    void existsByEmail_and_existsByUsername() {
        var id = unique();
        var user = User.builder().username("asmith-" + id).email("asmith-" + id + "@example.com")
                .displayName("Alice Smith").passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("asmith-" + id + "@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("asmith-" + id)).isTrue();
    }

    @Test
    void uniqueEmailConstraint_isEnforced() {
        var id = unique();
        var u1 = User.builder().username("user1-" + id).email("dup-" + id + "@example.com")
                .displayName("User One").passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        userRepository.save(u1);

        var u2 = User.builder().username("user2-" + id).email("dup-" + id + "@example.com")
                .displayName("User Two").passwordHash("").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            userRepository.saveAndFlush(u2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

