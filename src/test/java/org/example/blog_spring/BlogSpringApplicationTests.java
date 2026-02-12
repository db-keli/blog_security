package org.example.blog_spring;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disabled until a dedicated test database/profile is configured")
class BlogSpringApplicationTests {

    @Test
    void contextLoads() {
    }
}
