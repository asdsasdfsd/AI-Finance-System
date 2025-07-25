package org.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtAuthenticationTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    @DisplayName("Should generate and validate JWT token with correct subject")
    void testGenerateAndValidateJwtToken() {
        // Arrange
        String subject = "testuser";
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Act
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        // Assert
        assertThat(claims.getSubject()).isEqualTo(subject);
    }

    @Test
    @DisplayName("Should fail to parse JWT token with invalid secret")
    void testParseJwtTokenWithInvalidSecret() {
        // Arrange
        String subject = "testuser";
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Use a wrong secret
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThatThrownBy(() ->
                Jwts.parserBuilder()
                        .setSigningKey(wrongKey)
                        .build()
                        .parseClaimsJws(jwt)
        ).isInstanceOf(JwtException.class);
    }
}