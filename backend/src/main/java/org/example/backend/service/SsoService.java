// backend/src/main/java/org/example/backend/service/SsoService.java
package org.example.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.example.backend.application.service.UserApplicationService;
import org.example.backend.application.service.CompanyApplicationService;
import org.example.backend.application.dto.CreateUserCommand;
import org.example.backend.application.dto.CreateCompanyCommand;
import org.example.backend.application.dto.UserDTO;
import org.example.backend.application.dto.CompanyDTO;
import org.example.backend.exception.UnauthorizedException;
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

/**
 * SSO Service - DDD版本
 * 
 * 使用DDD应用服务进行用户和公司管理
 */
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

    private final UserApplicationService userApplicationService;
    private final CompanyApplicationService companyApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    public SsoService(UserApplicationService userApplicationService,
                     CompanyApplicationService companyApplicationService,
                     PasswordEncoder passwordEncoder) {
        this.userApplicationService = userApplicationService;
        this.companyApplicationService = companyApplicationService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Process SSO login by exchanging authorization code for access token,
     * then retrieving user information and either logging in or provisioning a new user
     *
     * @param code Authorization code from MS SSO
     * @param state State parameter for verification
     * @param provisioningFlags Map to store flags indicating if new user/company was created
     * @return The authenticated User DTO
     */
    @Transactional
    public UserDTO processSsoLogin(String code, String state, Map<String, Boolean> provisioningFlags) {
        try {
            // Exchange authorization code for access token
            String accessToken = getAccessToken(code);
            
            // Get user info from Microsoft
            Map<String, Object> userInfo = getUserInfo(accessToken);
            
            // Debug log - print full user info for troubleshooting
            System.out.println("Microsoft SSO User Info: " + userInfo);
            
            // Process user information
            String externalId = (String) userInfo.get("sub");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            
            // Check account type in a more robust way
            if (userInfo.containsKey("tid")) {
                // 'tid' Indicates the tenant ID
                String tenantId = (String) userInfo.get("tid");
                System.out.println("Microsoft Tenant ID: " + tenantId);
                
                // 9188040d-6c67-4c5b-b112-36a304b66dad is Microsoft's personal account tenant ID
                if ("9188040d-6c67-4c5b-b112-36a304b66dad".equals(tenantId)) {
                    System.out.println("Personal Microsoft account detected: " + email);
                    throw new UnauthorizedException("Please use your organisation's Microsoft account to log in instead of personal account.");
                }
            } else {
                // If we can still identify the user with sub/email but don't have tenant info
                // Let's allow login but log the issue
                System.out.println("Warning: Could not find tenant ID in Microsoft response, but continuing with login. User: " + email);
            }
            
            // Check if user exists - 使用DDD应用服务
            UserDTO existingUser = null;
            try {
                existingUser = userApplicationService.getUserByExternalId(externalId);
            } catch (Exception e) {
                // User not found, will create new one
            }
            
            if (existingUser != null) {
                // Update existing user if needed - 使用DDD应用服务
                boolean emailChanged = !email.equals(existingUser.getEmail());
                boolean nameChanged = (name != null && !name.equals(existingUser.getFullName())) || 
                                      (name == null && existingUser.getFullName() != null);
                
                if (emailChanged || nameChanged) {
                    // Note: DDD应用服务可能需要添加updateSsoUser方法
                    // 暂时记录但不更新，避免破坏现有数据
                    System.out.println("SSO user info changed but not updating: " + existingUser.getUsername());
                }
                return existingUser;
            } else {
                // Set flag indicating new user being created
                provisioningFlags.put("newUserCreated", true);
                
                // Provision new user (and company if needed) - 使用DDD应用服务
                UserDTO newUser = provisionNewUser(externalId, email, name, userInfo, provisioningFlags);
                return newUser;
            }
        } catch (UnauthorizedException e) {
            // Just rethrow these as they're already formatted properly
            throw e;
        } catch (Exception e) {
            // Log the full exception for troubleshooting
            System.err.println("Error in SSO login process: " + e.getMessage());
            e.printStackTrace();
            throw new UnauthorizedException("SSO authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method for compatibility with existing code
     */
    @Transactional
    public UserDTO processSsoLogin(String code, String state) {
        Map<String, Boolean> provisioningFlags = new HashMap<>();
        return processSsoLogin(code, state, provisioningFlags);
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
     * Also creates company if needed
     * 
     * @param externalId External ID from SSO provider
     * @param email User email
     * @param fullName User full name
     * @param userInfo Additional user information from Microsoft
     * @param provisioningFlags Map to store flags indicating if new user/company was created
     * @return Newly created User DTO
     */
    private UserDTO provisionNewUser(String externalId, String email, String fullName, 
            Map<String, Object> userInfo, Map<String, Boolean> provisioningFlags) {
        // Extract domain from email
        String domain = email.substring(email.indexOf('@') + 1);
        
        // Find company by email domain or create new one - 使用DDD应用服务
        CompanyDTO company = findOrCreateCompany(domain, email, userInfo, provisioningFlags);
        
        // Generate username (email prefix)
        String username = email.substring(0, email.indexOf('@')) + "_sso";
        int counter = 1;
        while (userApplicationService.existsByUsername(username)) {
            username = email.substring(0, email.indexOf('@')) + "_sso" + counter++;
        }
        
        // Determine user role based on whether they created the company
        boolean isNewCompany = provisioningFlags.getOrDefault("newCompanyCreated", false);
        String roleName = isNewCompany ? "COMPANY_ADMIN" : "USER";
        
        // Create new user - 使用DDD应用服务
        CreateUserCommand userCommand = CreateUserCommand.builder()
                .username(username)
                .email(email)
                .password(UUID.randomUUID().toString()) // SSO用户不需要密码
                .fullName(fullName)
                .enabled(true)
                .companyId(company.getCompanyId())
                .preferredLanguage("zh-CN")
                .timezone("Asia/Shanghai")
                .roleNames(Set.of(roleName))
                .build();
        
        UserDTO newUser = userApplicationService.createSsoUser(
            username, email, fullName, externalId, company.getCompanyId(), roleName
        );
        
        return newUser;
    }
    
    /**
     * Find existing company by domain or create a new one
     * 
     * @param domain Email domain
     * @param email User email
     * @param userInfo Additional user information from Microsoft
     * @param provisioningFlags Map to store flags indicating if new company was created
     * @return Existing or newly created company DTO
     */
    private CompanyDTO findOrCreateCompany(String domain, String email, Map<String, Object> userInfo,
            Map<String, Boolean> provisioningFlags) {
        // Try to find existing company by email domain - 使用DDD应用服务
        Optional<CompanyDTO> existingCompany = companyApplicationService.findByEmailDomain(domain);
        
        if (existingCompany.isPresent()) {
            return existingCompany.get();
        }
        
        // Set flag indicating new company being created
        provisioningFlags.put("newCompanyCreated", true);
        
        // Try to get organization name from Microsoft claims
        String companyName = domain;
        if (userInfo.containsKey("organization") && userInfo.get("organization") instanceof String) {
            companyName = (String) userInfo.get("organization");
        } else if (userInfo.containsKey("tenant") && userInfo.get("tenant") instanceof Map) {
            Map<String, Object> tenant = (Map<String, Object>) userInfo.get("tenant");
            if (tenant.containsKey("name")) {
                companyName = (String) tenant.get("name");
            }
        }
        
        // Create new company - 使用DDD应用服务
        CreateCompanyCommand companyCommand = CreateCompanyCommand.builder()
                .companyName(companyName)
                .email(email)
                .address("Auto-provisioned")
                .city("Auto-provisioned")
                .stateProvince("Auto-provisioned")
                .postalCode("00000")
                .website("https://" + domain)
                .registrationNumber("AUTO-" + UUID.randomUUID().toString().substring(0, 8))
                .taxId("AUTO-" + UUID.randomUUID().toString().substring(0, 8))
                .fiscalYearStart("01-01")
                .defaultCurrency("USD")
                .maxUsers(100)
                .createdBy(1) // 系统创建
                .build();
        
        return companyApplicationService.createCompany(companyCommand);
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