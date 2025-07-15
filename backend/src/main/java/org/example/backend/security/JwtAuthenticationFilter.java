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

        String requestURI = request.getRequestURI();
        
        // Skip JWT filter for public endpoints
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Skipping JWT validation for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // For development mode, allow bypassing authentication for test endpoints
        if (isDevelopmentMode() && requestURI.startsWith("/api/debug/")) {
            logger.debug("Skipping JWT validation for debug endpoint in dev mode: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;

        // Check if Authorization header exists and is properly formatted
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwt = requestTokenHeader.substring(7);
            
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from JWT: {}", username);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token has expired: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-001", "JWT token has expired. Please login again.");
                return;
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-002", "Invalid JWT token format.");
                return;
            } catch (UnsupportedJwtException e) {
                logger.error("JWT token is unsupported: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-003", "Unsupported JWT token.");
                return;
            } catch (SignatureException e) {
                logger.error("Invalid JWT signature: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-004", "Invalid JWT signature.");
                return;
            } catch (IllegalArgumentException e) {
                logger.error("JWT claims string is empty: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-005", "Empty JWT claims.");
                return;
            } catch (Exception e) {
                logger.error("JWT token error: {}", e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-006", "JWT token processing error.");
                return;
            }
        } else {
            logger.warn("No Authorization header found or invalid format for URI: {}", requestURI);
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "AUTH-SEC-007", "Authorization header required.");
            return;
        }

        // If we found a valid token and there's no authentication yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate the token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Successfully authenticated user: {}", username);
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "AUTH-SEC-008", "JWT token validation failed.");
                    return;
                }
            } catch (Exception e) {
                logger.error("Authentication error for user {}: {}", username, e.getMessage());
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "AUTH-SEC-009", "User authentication failed.");
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
               requestURI.equals("/api/test/hello") ||  // Allow health check
               requestURI.contains("/h2-console") ||
               requestURI.contains("/actuator/health") ||
               requestURI.equals("/") ||
               requestURI.contains("/error");
    }
    
    /**
     * Check if application is running in development mode
     * 
     * @return true if in development mode
     */
    private boolean isDevelopmentMode() {
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        return activeProfiles.contains("dev") || activeProfiles.contains("development");
    }
    
    /**
     * Write error response in JSON format
     * 
     * @param response HTTP response
     * @param status HTTP status code
     * @param errorCode Application error code
     * @param message Error message
     * @throws IOException if writing fails
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String errorCode, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"status\":\"error\",\"error\":{\"code\":\"%s\",\"message\":\"%s\"}}",
            errorCode, message
        );
        
        response.getWriter().write(jsonResponse);
    }
}