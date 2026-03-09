package org.example.blog_spring.web.rest;

import org.example.blog_spring.dto.ApiResponse;
import org.example.blog_spring.security.SecurityMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SecurityMetrics securityMetrics;

    public AdminController(SecurityMetrics securityMetrics) {
        this.securityMetrics = securityMetrics;
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> health() {
        var response = ApiResponse.success(HttpStatus.OK, "Admin endpoint reachable", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/security-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> metrics() {
        var data = securityMetrics.snapshot();
        var response = ApiResponse.success(HttpStatus.OK, "Security metrics snapshot", data);
        return ResponseEntity.ok(response);
    }
}

