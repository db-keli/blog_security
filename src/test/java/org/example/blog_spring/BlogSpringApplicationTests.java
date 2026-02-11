package org.example.blog_spring;

import org.example.blog_spring.config.TestContainersConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires Docker for Testcontainers; run with: mvn test -Dtest=BlogSpringApplicationTests")
class BlogSpringApplicationTests extends TestContainersConfig {

    @Test
    void contextLoads() {
    }
}
