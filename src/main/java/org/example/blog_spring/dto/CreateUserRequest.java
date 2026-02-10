package org.example.blog_spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.blog_spring.validation.UniqueEmail;

public record CreateUserRequest(
                @NotBlank @Size(min = 3, max = 50) String username,

                @NotBlank @Email @Size(max = 255) @UniqueEmail String email,

                @NotBlank @Size(max = 100) String fullName) {
}

