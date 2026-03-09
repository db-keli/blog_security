package org.example.blog_spring.config;

import java.util.List;

import org.example.blog_spring.security.CustomOAuth2UserService;
import org.example.blog_spring.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CustomOAuth2UserService customOAuth2UserService,
                        ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> clientRegistrationRepositoryProvider)
                        throws Exception {
                http.csrf(csrf -> csrf
                                .ignoringRequestMatchers("/api/**", "/graphql", "/v3/api-docs/**",
                                                "/swagger-ui/**", "/swagger-ui.html",
                                                "/actuator/**")
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                                .cors(Customizer.withDefaults())
                                .httpBasic(Customizer.withDefaults())
                                // Allow sessions when required so OAuth2 login can store
                                // the authorization request and SecurityContext while the
                                // REST API remains effectively stateless for JWT requests.
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/api/auth/login",
                                                                "/api/auth/register",
                                                                // OAuth2 login endpoints
                                                                "/oauth2/**", "/login/**",
                                                                "/login/oauth2/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/actuator/health")
                                                .permitAll()
                                                // Example restricted endpoints by role
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/posts/**")
                                                .hasAnyRole("ADMIN", "AUTHOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/posts/**")
                                                .hasAnyRole("ADMIN", "AUTHOR")
                                                .requestMatchers(HttpMethod.DELETE, "/api/posts/**")
                                                .hasAnyRole("ADMIN", "AUTHOR")
                                                .requestMatchers(HttpMethod.GET, "/api/posts/**")
                                                .permitAll().anyRequest().authenticated());
                var clientRegistrationRepository =
                                clientRegistrationRepositoryProvider.getIfAvailable();
                if (clientRegistrationRepository != null) {
                        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                                        userInfo -> userInfo.userService(customOAuth2UserService))
                                        .defaultSuccessUrl("/swagger-ui.html", true)
                                        .failureUrl("/swagger-ui.html?oauth2_error=true"));
                }
                http.addFilterBefore(jwtAuthenticationFilter,
                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Global CORS configuration so external frontends (React, JavaFX, etc.) can interact safely
         * with the API.
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of("http://localhost:3000", // typical React
                                                                                 // dev server
                                "http://localhost:5173", // Vite
                                "http://localhost:8080" // other local clients
                ));
                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type",
                                "X-Auth-Token", "X-Requested-With"));
                configuration.setExposedHeaders(List.of("X-Auth-Token"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}

