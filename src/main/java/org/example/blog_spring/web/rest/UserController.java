package org.example.blog_spring.web.rest;

import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.dto.CreateUserRequest;
import org.example.blog_spring.dto.UpdateUserRequest;
import org.example.blog_spring.dto.UserDto;
import org.example.blog_spring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        var userDto = userService.createUser(request);
        var response =
                ApiResponse.success(HttpStatus.CREATED, "User created successfully", userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by id")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
        var userDto = userService.getUser(id);
        var response = ApiResponse.success(HttpStatus.OK, "User retrieved successfully", userDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List users with pagination")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getUsers(Pageable pageable) {
        var users = userService.getUsers(pageable);
        var response = ApiResponse.success(HttpStatus.OK, "Users retrieved successfully", users);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        var updated = userService.updateUser(id, request);
        var response = ApiResponse.success(HttpStatus.OK, "User updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        var response =
                ApiResponse.<Void>success(HttpStatus.NO_CONTENT, "User deleted successfully", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}

