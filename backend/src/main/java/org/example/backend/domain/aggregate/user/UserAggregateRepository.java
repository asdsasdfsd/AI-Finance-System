// backend/src/main/java/org/example/backend/domain/aggregate/user/UserAggregateRepository.java
package org.example.backend.domain.aggregate.user;

import org.example.backend.domain.valueobject.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Aggregate Repository - DDD compliant
 * 
 * Responsibilities:
 * 1. Manage user aggregate persistence within tenant boundaries
 * 2. Support multi-tenant user queries
 * 3. Handle authentication and authorization queries
 * 4. Provide role-based and department-based queries
 */
@Repository
public interface UserAggregateRepository extends JpaRepository<UserAggregate, Integer> {
    
    // ========== Basic User Queries ==========
    
    /**
     * Find user by username
     */
    Optional<UserAggregate> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<UserAggregate> findByEmail(String email);
    
    /**
     * Find user by external ID (SSO)
     */
    Optional<UserAggregate> findByExternalId(String externalId);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    // ========== Tenant-based Queries ==========
    
    /**
     * Find all users within a tenant
     */
    List<UserAggregate> findByTenantIdOrderByFullNameAsc(TenantId tenantId);
    
    /**
     * Find users by tenant
     */
    List<UserAggregate> findByTenantId(TenantId tenantId);
    
    /**
     * Count users by tenant
     */
    long countByTenantId(TenantId tenantId);
    
    /**
     * Find active users by tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.enabled = true ORDER BY u.fullName")
    List<UserAggregate> findActiveUsersByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find users by tenant and username pattern
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.username LIKE CONCAT(:username, '%')")
    List<UserAggregate> findByTenantIdAndUsernameStartingWith(@Param("tenantId") TenantId tenantId, 
                                                             @Param("username") String username);
    
    // ========== Department-based Queries ==========
    
    /**
     * Find users by tenant and department
     */
    List<UserAggregate> findByTenantIdAndDepartmentId(TenantId tenantId, Integer departmentId);
    
    /**
     * Find users without department in tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.departmentId IS NULL")
    List<UserAggregate> findByTenantIdAndDepartmentIdIsNull(@Param("tenantId") TenantId tenantId);
    
    /**
     * Count users by department
     */
    long countByTenantIdAndDepartmentId(TenantId tenantId, Integer departmentId);
    
    // ========== Role-based Queries ==========
    
    /**
     * Find users by tenant and role
     */
    @Query("SELECT u FROM UserAggregate u JOIN u.roles r WHERE u.tenantId = :tenantId AND r.name = :roleName")
    List<UserAggregate> findByTenantIdAndRole(@Param("tenantId") TenantId tenantId, 
                                            @Param("roleName") String roleName);
    
    /**
     * Find users with specific role in tenant
     */
    @Query("SELECT u FROM UserAggregate u JOIN u.roles r WHERE u.tenantId = :tenantId AND r.name IN :roleNames")
    List<UserAggregate> findByTenantIdAndRoleIn(@Param("tenantId") TenantId tenantId, 
                                              @Param("roleNames") List<String> roleNames);
    
    /**
     * Count users by role in tenant
     */
    @Query("SELECT COUNT(u) FROM UserAggregate u JOIN u.roles r WHERE u.tenantId = :tenantId AND r.name = :roleName")
    long countByTenantIdAndRole(@Param("tenantId") TenantId tenantId, 
                              @Param("roleName") String roleName);
    
    // ========== Search and Filter Queries ==========
    
    /**
     * Search users by name or email within tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<UserAggregate> searchByNameOrEmail(@Param("tenantId") TenantId tenantId, 
                                          @Param("searchTerm") String searchTerm);
    
    /**
     * Find users by multiple criteria
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:departmentId IS NULL OR u.departmentId = :departmentId) AND " +
           "(:searchTerm IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<UserAggregate> findByMultipleCriteria(@Param("tenantId") TenantId tenantId,
                                             @Param("enabled") Boolean enabled,
                                             @Param("departmentId") Integer departmentId,
                                             @Param("searchTerm") String searchTerm);
    
    // ========== Authentication and Security Queries ==========
    
    /**
     * Find locked users in tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.lockedUntil > :now")
    List<UserAggregate> findLockedUsers(@Param("tenantId") TenantId tenantId, 
                                      @Param("now") LocalDateTime now);
    
    /**
     * Find users with failed login attempts
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.failedLoginAttempts >= :threshold")
    List<UserAggregate> findUsersWithFailedAttempts(@Param("tenantId") TenantId tenantId, 
                                                   @Param("threshold") Integer threshold);
    
    /**
     * Find SSO users in tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.externalId IS NOT NULL")
    List<UserAggregate> findSsoUsers(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find users with expired passwords
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND " +
           "u.externalId IS NULL AND u.passwordChangedAt < :expiryDate")
    List<UserAggregate> findUsersWithExpiredPasswords(@Param("tenantId") TenantId tenantId, 
                                                     @Param("expiryDate") LocalDateTime expiryDate);
    
    // ========== Activity and Audit Queries ==========
    
    /**
     * Find users who logged in recently
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.lastLogin > :since ORDER BY u.lastLogin DESC")
    List<UserAggregate> findRecentlyActiveUsers(@Param("tenantId") TenantId tenantId, 
                                              @Param("since") LocalDateTime since);
    
    /**
     * Find users who never logged in
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.lastLogin IS NULL")
    List<UserAggregate> findUsersNeverLoggedIn(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find recently created users
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.createdAt > :since ORDER BY u.createdAt DESC")
    List<UserAggregate> findRecentlyCreatedUsers(@Param("tenantId") TenantId tenantId, 
                                               @Param("since") LocalDateTime since);
    
    /**
     * Find recently updated users
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.updatedAt > :since ORDER BY u.updatedAt DESC")
    List<UserAggregate> findRecentlyUpdatedUsers(@Param("tenantId") TenantId tenantId, 
                                               @Param("since") LocalDateTime since);
    
    // ========== Statistics Queries ==========
    
    /**
     * Get user creation statistics by month
     */
    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) " +
           "FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.createdAt >= :startDate " +
           "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
           "ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> getUserCreationStats(@Param("tenantId") TenantId tenantId, 
                                      @Param("startDate") LocalDateTime startDate);
    
    /**
     * Count enabled vs disabled users
     */
    @Query("SELECT u.enabled, COUNT(u) FROM UserAggregate u WHERE u.tenantId = :tenantId GROUP BY u.enabled")
    List<Object[]> countUsersByEnabledStatus(@Param("tenantId") TenantId tenantId);
    
    /**
     * Count users by role
     */
    @Query("SELECT r.name, COUNT(u) FROM UserAggregate u JOIN u.roles r WHERE u.tenantId = :tenantId GROUP BY r.name")
    List<Object[]> countUsersByRole(@Param("tenantId") TenantId tenantId);
    
    // ========== Validation Queries ==========
    
    /**
     * Check if username exists in tenant
     */
    @Query("SELECT COUNT(u) > 0 FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.username = :username")
    boolean existsByTenantIdAndUsername(@Param("tenantId") TenantId tenantId, 
                                      @Param("username") String username);
    
    /**
     * Check if email exists in tenant
     */
    @Query("SELECT COUNT(u) > 0 FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.email = :email")
    boolean existsByTenantIdAndEmail(@Param("tenantId") TenantId tenantId, 
                                   @Param("email") String email);
    
    /**
     * Find user by username and tenant
     */
    @Query("SELECT u FROM UserAggregate u WHERE u.tenantId = :tenantId AND u.username = :username")
    Optional<UserAggregate> findByTenantIdAndUsername(@Param("tenantId") TenantId tenantId, 
                                                    @Param("username") String username);
}