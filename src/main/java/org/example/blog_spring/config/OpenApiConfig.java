package org.example.blog_spring.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Blogging Platform API", version = "v1",
                description = "REST API for managing users, posts, comments, tags, and reviews."))
public class OpenApiConfig {
}

