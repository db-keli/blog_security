package org.example.blog_spring.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.example.blog_spring.domain.User;
import org.example.blog_spring.domain.UserRole;
import org.example.blog_spring.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final List<String> adminEmails;

    public CustomOAuth2UserService(UserRepository userRepository,
            @Value("${security.oauth2.admin-emails:}") List<String> adminEmails) {
        this.userRepository = userRepository;
        this.adminEmails = adminEmails;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.getOrDefault("email", "");
        String name = (String) attributes.getOrDefault("name", email);

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not provided by OAuth2 provider");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            UserRole role = resolveRoleForEmail(email);
            User u = new User();

            String username = email;
            if (username.length() > 50) {
                username = username.substring(0, 50);
            }

            u.setUsername(username);
            u.setEmail(email);
            u.setDisplayName(name);
            u.setPasswordHash(""); // not used for OAuth2 logins
            u.setRole(role);
            Instant now = Instant.now();
            u.setCreatedAt(now);
            u.setUpdatedAt(now);

            log.info("Creating new OAuth2 user '{}' with username '{}' and role {}", email,
                    username, role);
            return userRepository.save(u);
        });

        UserRole role = user.getRole();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

        return new DefaultOAuth2User(List.of(authority), attributes, "email");
    }

    private UserRole resolveRoleForEmail(String email) {
        if (adminEmails != null && adminEmails.stream().anyMatch(e -> e.equalsIgnoreCase(email))) {
            return UserRole.ADMIN;
        }
        return UserRole.READER;
    }
}

