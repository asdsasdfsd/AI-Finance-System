// backend/src/main/java/org/example/backend/service/SsoService.java
package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Role;
import org.example.backend.model.User;
import org.example.backend.repository.CompanyRepository;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SsoService {

    @Value("${sso.client-id}")
    private String clientId;

    @Value("${sso.client-secret}")
    private String clientSecret;

    @Value("${sso.token-url}")
    private String tokenUrl;

    @Value("${sso.user-info-url}")
    private String userInfoUrl;

    @Value("${sso.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    public SsoService(UserRepository userRepository,
                     CompanyRepository companyRepository,
                     RoleRepository roleRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Process SSO login by exchanging authorization code for access token,
     * then retrieving user information and either logging in or provisioning a new user
     *
     * @param code Authorization code from MS SSO
     * @param state State parameter for verification
     * @return The authenticated User
     */
    @Transactional
    public User processSsoLogin(String code, String state) {
        // Exchange authorization code for access token
        String accessToken = getAccessToken(code);
        
        // Get user info from Microsoft
        Map<String, Object> userInfo = getUserInfo(accessToken);
        
        // Process user information
        String externalId = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        
        // Check if user exists
        Optional<User> existingUser = userRepository.findByExternalId(externalId);
        
        if (existingUser.isPresent()) {
            // Update existing user if needed
            User user = existingUser.get();
            // Update information if changed
            if (!user.getEmail().equals(email) || !user.getFullName().equals(name)) {
                user.setEmail(email);
                user.setFullName(name);
                user.setUpdatedAt(LocalDateTime.now());
                user = userRepository.save(user);
            }
            return user;
        } else {
            // Provision new user
            return provisionNewUser(externalId, email, name);
        }
    }
    
    /**
     * Exchange authorization code for access token
     * 
     * @param code Authorization code
     * @return Access token
     */
    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        map.add("grant_type", "authorization_code");
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        
        return (String) response.getBody().get("access_token");
    }
    
    /**
     * Get user information using access token
     * 
     * @param accessToken Access token
     * @return User information
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
        
        return response.getBody();
    }
    
    /**
     * Provision a new user from SSO information
     * 
     * @param externalId External ID from SSO provider
     * @param email User email
     * @param fullName User full name
     * @return Newly created User
     */
    private User provisionNewUser(String externalId, String email, String fullName) {
        // Find company by email domain (simple approach)
        String domain = email.substring(email.indexOf('@') + 1);
        Optional<Company> company = companyRepository.findByEmailDomain(domain);
        
        if (company.isEmpty()) {
            throw new IllegalStateException("Cannot provision user: no company found for domain " + domain);
        }
        
        // Generate username (email prefix)
        String username = email.substring(0, email.indexOf('@')) + "_sso";
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = email.substring(0, email.indexOf('@')) + "_sso" + counter++;
        }
        
        // Find default SSO user role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setExternalId(externalId);
        user.setEmail(email);
        user.setFullName(fullName);
        // Generate a random secure password that won't be used (SSO login only)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setCompany(company.get());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    /**
     * Get SSO login URL
     *
     * @return URL to redirect user for SSO login
     */
    public String getSsoLoginUrl() {
        // This URL should match your SSO provider's authorization endpoint
        return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" +
               "?client_id=" + clientId +
               "&response_type=code" +
               "&redirect_uri=" + redirectUri +
               "&response_mode=query" +
               "&scope=openid profile email" +
               "&state=" + UUID.randomUUID().toString();
    }
}