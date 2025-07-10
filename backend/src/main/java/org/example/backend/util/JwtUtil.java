// backend/src/main/java/org/example/backend/util/JwtUtil.java
package org.example.backend.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Extract username from token
     * 
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract company ID from token
     * 
     * @param token JWT token
     * @return Company ID
     */
    public Integer extractCompanyId(String token) {
        return extractClaim(token, claims -> {
            Object companyId = claims.get("companyId");
            if (companyId instanceof Integer) {
                return (Integer) companyId;
            } else if (companyId instanceof String) {
                try {
                    return Integer.parseInt((String) companyId);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT token
     * @return User ID
     */
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return (Integer) userId;
            } else if (userId instanceof String) {
                try {
                    return Integer.parseInt((String) userId);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * Extract user full name from token
     * 
     * @param token JWT token
     * @return Full name
     */
    public String extractFullName(String token) {
        return extractClaim(token, claims -> (String) claims.get("fullName"));
    }

    /**
     * Extract expiration date from token
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from token
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     * 
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate token for user with company and user information
     * 
     * @param userDetails User details
     * @param companyId Company ID
     * @param userId User ID
     * @param fullName User full name
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails, Integer companyId, Integer userId, String fullName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("companyId", companyId);
        claims.put("userId", userId);
        claims.put("fullName", fullName);
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate token for user (backward compatibility)
     * 
     * @param userDetails User details
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Use default values for backward compatibility
        claims.put("companyId", 1);
        claims.put("userId", 1);
        claims.put("fullName", userDetails.getUsername());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create token with claims and subject
     * 
     * @param claims Additional claims
     * @param subject Subject (username)
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Validate token for user
     * 
     * @param token JWT token
     * @param userDetails User details
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Get token expiration time in milliseconds
     * 
     * @return Expiration time
     */
    public long getExpirationTime() {
        return expiration;
    }

    /**
     * Validate token and check if it belongs to the specified company
     * 
     * @param token JWT token
     * @param userDetails User details
     * @param expectedCompanyId Expected company ID
     * @return true if valid and belongs to company, false otherwise
     */
    public Boolean validateTokenAndCompany(String token, UserDetails userDetails, Integer expectedCompanyId) {
        if (!validateToken(token, userDetails)) {
            return false;
        }
        
        Integer tokenCompanyId = extractCompanyId(token);
        return tokenCompanyId != null && tokenCompanyId.equals(expectedCompanyId);
    }
}