package org.example.blog_spring.web.rest;

import java.util.Map;

import org.example.blog_spring.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple CSRF demo for form-like requests.
 *
 * CSRF is enabled for this controller (because it is not under /api/**).
 */
@RestController
@RequestMapping("/csrf-demo")
public class CsrfDemoController {

        /**
         * Returns the current CSRF token so a browser client can embed it in a form. Spring
         * Security exposes the token as a request attribute named "_csrf".
         */
        @GetMapping("/token")
        public ResponseEntity<ApiResponse<Map<String, String>>> getToken(
                        @RequestAttribute(name = "_csrf") CsrfToken token) {
                var body = Map.of("parameterName", token.getParameterName(), "headerName",
                                token.getHeaderName(), "token", token.getToken());
                var response = ApiResponse.success(HttpStatus.OK, "CSRF token for demo form", body);
                return ResponseEntity.ok(response);
        }

        /**
         * Sample "form" endpoint protected by CSRF. A browser client must send the CSRF token
         * either as a header or form parameter.
         */
        @PostMapping("/submit")
        public ResponseEntity<ApiResponse<Map<String, Object>>> submit(
                        @RequestBody(required = false) Map<String, Object> payload) {
                Map<String, Object> body = Map.of("received", payload == null ? Map.of() : payload);
                var response = ApiResponse.success(HttpStatus.OK,
                                "CSRF-protected submission accepted", body);
                return ResponseEntity.ok(response);
        }
}

