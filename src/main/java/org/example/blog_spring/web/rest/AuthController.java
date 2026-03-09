package org.example.blog_spring.web.rest;

import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.LoginRequest;
import org.example.blog_spring.dto.LoginResponse;
import org.example.blog_spring.dto.RegisterRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication APIs without Spring Security")
public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping("/register")
        @Operation(summary = "Register a new user account")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                                        description = "User registered successfully",
                                        content = @Content(schema = @Schema(
                                                        implementation = UserDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                                        description = "Invalid input or validation failure"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                                        description = "Email or username already exists")})
        public ResponseEntity<ApiResponse<UserDto>> register(
                        @Valid @RequestBody RegisterRequest request) {
                var userDto = authService.register(request);
                var response = ApiResponse.success(HttpStatus.CREATED,
                                "User registered successfully", userDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/login")
        @Operation(summary = "Authenticate a user and return a simple token")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                        description = "Login successful",
                                        content = @Content(schema = @Schema(
                                                        implementation = LoginResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                                        description = "Invalid input or validation failure"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                                        description = "Invalid email or password")})
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request) {
                var login = authService.login(request);
                var response = ApiResponse.success(HttpStatus.OK, "Login successful", login);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/logout")
        @Operation(summary = "Log out a user by invalidating the token")
        @ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200", description = "Logout successful")})
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader(name = "X-Auth-Token", required = false) String token) {
                authService.logout(token);
                var response = ApiResponse.<Void>success(HttpStatus.OK, "Logout successful", null);
                return ResponseEntity.ok(response);
        }
}

