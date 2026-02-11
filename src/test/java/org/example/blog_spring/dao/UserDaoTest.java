package org.example.blog_spring.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.blog_spring.config.TestContainersConfig;
import org.example.blog_spring.domain.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires Docker for Testcontainers; run with Docker available")
class UserDaoTest extends TestContainersConfig {

    @Autowired
    private UserDao userDao;

    @Test
    void insert_setsGeneratedId() {
        var user = new User("newuser", "new@e.com", "New User");

        var saved = userDao.insert(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
    }

    @Test
    void findById_returnsUser_afterInsert() {
        var user = new User("finduser", "find@e.com", "Find User");
        var saved = userDao.insert(user);

        var found = userDao.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("finduser");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        var user = userDao.findById(999999L);

        assertThat(user).isEmpty();
    }

    @Test
    void findAll_returnsPagedResults() {
        var page = userDao.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void existsByEmail_returnsTrue_afterInsert() {
        var user = new User("emailuser", "unique@e.com", "Email User");
        userDao.insert(user);

        var exists = userDao.existsByEmail("unique@e.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenNotExists() {
        var exists =
                userDao.existsByEmail("nonexistent-" + System.currentTimeMillis() + "@example.com");

        assertThat(exists).isFalse();
    }
}
