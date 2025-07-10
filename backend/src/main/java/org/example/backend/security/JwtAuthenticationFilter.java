// backend/src/main/java/org/example/backend/security/JwtAuthenticationFilter.java
package org.example.backend.security;

import java.io.IOException;

import org.example.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get request URI
        String requestURI = request.getRequestURI();
        
        // Skip JWT filter for public endpoints only
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Skipping JWT validation for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Check if the Authorization header exists and has the correct format
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (ExpiredJwtException e) {
                logger.info("JWT token has expired: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("X-JWT-Expired", "true");
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-002\",\"message\":\"Session has expired. Please login again.\"}}");
                return;
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-003\",\"message\":\"Invalid token format.\"}}");
                return;
            } catch (UnsupportedJwtException e) {
                logger.error("JWT token is unsupported: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-004\",\"message\":\"Unsupported token type.\"}}");
                return;
            } catch (SignatureException e) {
                logger.error("Invalid JWT signature: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-005\",\"message\":\"Invalid token signature.\"}}");
                return;
            } catch (IllegalArgumentException e) {
                logger.error("JWT claims string is empty: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-006\",\"message\":\"Invalid token claims.\"}}");
                return;
            } catch (Exception e) {
                logger.error("JWT token error: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-001\",\"message\":\"Token validation failed.\"}}");
                return;
            }
        } else {
            // No Authorization header found for protected endpoint
            logger.warn("Missing Authorization header for protected endpoint: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-007\",\"message\":\"Authorization header required.\"}}");
            return;
        }

        // If we found a valid token and there's no authentication yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate the token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Create authentication token with JWT as credentials for later access
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, jwt, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Successfully authenticated user: {}", username);
                } else {
                    logger.warn("Token validation failed for user: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-008\",\"message\":\"Token validation failed.\"}}");
                    return;
                }
            } catch (Exception e) {
                logger.error("Authentication error for user {}: {}", username, e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"error\",\"error\":{\"code\":\"AUTH-SEC-009\",\"message\":\"Authentication failed.\"}}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the requested endpoint is a public endpoint that doesn't require authentication
     * 
     * @param requestURI The request URI
     * @return true if it's a public endpoint
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.contains("/api/auth/") || 
            requestURI.contains("/api/public/") || 
            requestURI.contains("/api/sso/") || 
            requestURI.contains("/api/health") ||     // 新增
            requestURI.equals("/api/health") ||       // 新增
            requestURI.equals("/health") ||           // 新增
            requestURI.equals("/") ||
            requestURI.contains("/error") ||
            requestURI.contains("/actuator") ||
            requestURI.contains("/swagger") ||
            requestURI.contains("/v3/api-docs");
    }
}