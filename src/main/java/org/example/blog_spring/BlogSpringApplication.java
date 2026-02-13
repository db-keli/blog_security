package org.example.blog_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BlogSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogSpringApplication.class, args);
    }

}
