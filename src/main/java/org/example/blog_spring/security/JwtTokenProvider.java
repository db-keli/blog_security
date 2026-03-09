package org.example.blog_spring.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMs;

    public JwtTokenProvider(@Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.validity-ms}") long validityInMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMs = validityInMs;
    }

    public String createToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMs);

        List<String> roles =
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder().subject(userDetails.getUsername()).claim("roles", roles)
                .issuedAt(Date.from(now)).expiration(Date.from(expiry))
                .signWith(secretKey, Jwts.SIG.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            return false;
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public List<String> getRoles(String token) {
        Claims claims = parseClaims(token).getPayload();
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }
}

