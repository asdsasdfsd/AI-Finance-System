// backend/src/main/java/org/example/backend/domain/aggregate/report/ReportAggregateRepository.java
package org.example.backend.domain.aggregate.report;

import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Report Aggregate Repository
 * 
 * Responsibilities:
 * 1. Provide aggregate-focused persistence operations
 * 2. Support multi-tenant report queries
 * 3. Enable report filtering and searching
 * 4. Support AI analysis data queries
 */
@Repository
public interface ReportAggregateRepository extends JpaRepository<ReportAggregate, Integer> {
    
    // ========== Basic Queries ==========
    
    /**
     * Find report by ID within tenant boundary
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.reportId = :reportId AND r.tenantId = :tenantId")
    Optional<ReportAggregate> findByIdAndTenant(@Param("reportId") Integer reportId, 
                                               @Param("tenantId") TenantId tenantId);
    
    /**
     * Find all reports for a tenant ordered by creation date (newest first)
     */
    List<ReportAggregate> findByTenantIdOrderByCreatedAtDesc(TenantId tenantId);
    
    /**
     * Find reports by tenant and type
     */
    List<ReportAggregate> findByTenantIdAndReportTypeOrderByCreatedAtDesc(TenantId tenantId, 
                                                                         ReportType reportType);
    
    /**
     * Find reports by tenant and status
     */
    List<ReportAggregate> findByTenantIdAndStatusOrderByCreatedAtDesc(TenantId tenantId, 
                                                                     ReportStatus status);
    
    // ========== Date Range Queries ==========
    
    /**
     * Find reports by tenant and date range
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.startDate >= :startDate AND r.endDate <= :endDate " +
           "ORDER BY r.createdAt DESC")
    List<ReportAggregate> findByTenantAndDateRange(@Param("tenantId") TenantId tenantId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * Find reports created within specific time period
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<ReportAggregate> findByTenantAndCreatedSince(@Param("tenantId") TenantId tenantId,
                                                     @Param("since") LocalDateTime since);
    
    /**
     * Find reports by period overlap
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND NOT (r.endDate < :startDate OR r.startDate > :endDate) " +
           "ORDER BY r.startDate ASC")
    List<ReportAggregate> findByTenantAndPeriodOverlap(@Param("tenantId") TenantId tenantId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    // ========== Status-based Queries ==========
    
    /**
     * Find completed reports for tenant
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.status = 'COMPLETED' ORDER BY r.completedAt DESC")
    List<ReportAggregate> findCompletedReportsByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find failed reports for tenant
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.status = 'FAILED' ORDER BY r.createdAt DESC")
    List<ReportAggregate> findFailedReportsByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find reports currently generating
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.status = 'GENERATING' ORDER BY r.createdAt ASC")
    List<ReportAggregate> findGeneratingReportsByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find stuck/hanging reports (generating for too long)
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.status = 'GENERATING' " +
           "AND r.createdAt < :cutoffTime")
    List<ReportAggregate> findStuckReports(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // ========== AI Analysis Queries ==========
    
    /**
     * Find reports with AI analysis enabled
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.aiAnalysisEnabled = true ORDER BY r.createdAt DESC")
    List<ReportAggregate> findAIEnabledReportsByTenant(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find reports ready for AI analysis
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.aiAnalysisEnabled = true AND r.aiAnalysisStatus = 'READY' " +
           "ORDER BY r.createdAt ASC")
    List<ReportAggregate> findReportsReadyForAI(@Param("tenantId") TenantId tenantId);
    
    /**
     * Find reports with completed AI analysis
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.aiAnalysisEnabled = true AND r.aiAnalysisStatus = 'COMPLETED' " +
           "ORDER BY r.createdAt DESC")
    List<ReportAggregate> findReportsWithCompletedAI(@Param("tenantId") TenantId tenantId);
    
    // ========== Search and Filter Queries ==========
    
    /**
     * Search reports by name (case-insensitive)
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND LOWER(r.reportName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY r.createdAt DESC")
    List<ReportAggregate> searchByName(@Param("tenantId") TenantId tenantId, 
                                      @Param("searchTerm") String searchTerm);
    
    /**
     * Find reports by multiple criteria
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND (:reportType IS NULL OR r.reportType = :reportType) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:startDate IS NULL OR r.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.endDate <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<ReportAggregate> findByMultipleCriteria(@Param("tenantId") TenantId tenantId,
                                               @Param("reportType") ReportType reportType,
                                               @Param("status") ReportStatus status,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Find recent reports (last N days)
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<ReportAggregate> findRecentReports(@Param("tenantId") TenantId tenantId,
                                          @Param("since") LocalDateTime since);
    
    // ========== Statistics Queries ==========
    
    /**
     * Count reports by status for tenant
     */
    @Query("SELECT COUNT(r) FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.status = :status")
    long countByTenantAndStatus(@Param("tenantId") TenantId tenantId, 
                              @Param("status") ReportStatus status);
    
    /**
     * Count reports by type for tenant
     */
    @Query("SELECT COUNT(r) FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.reportType = :reportType")
    long countByTenantAndType(@Param("tenantId") TenantId tenantId, 
                            @Param("reportType") ReportType reportType);
    
    /**
     * Get report generation statistics by month
     */
    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r) " +
           "FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.createdAt >= :since " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
           "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> getMonthlyReportStats(@Param("tenantId") TenantId tenantId,
                                       @Param("since") LocalDateTime since);
    
    /**
     * Get total file size for tenant
     */
    @Query("SELECT COALESCE(SUM(r.fileSize), 0) FROM ReportAggregate r " +
           "WHERE r.tenantId = :tenantId AND r.fileSize IS NOT NULL")
    long getTotalFileSizeByTenant(@Param("tenantId") TenantId tenantId);
    
    // ========== Cleanup Queries ==========
    
    /**
     * Find old archived reports for cleanup
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.status = 'ARCHIVED' " +
           "AND r.updatedAt < :cutoffDate ORDER BY r.updatedAt ASC")
    List<ReportAggregate> findOldArchivedReports(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find failed reports older than specific date
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.status = 'FAILED' " +
           "AND r.createdAt < :cutoffDate ORDER BY r.createdAt ASC")
    List<ReportAggregate> findOldFailedReports(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // ========== User-specific Queries ==========
    
    /**
     * Find reports created by specific user
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.createdBy = :userId ORDER BY r.createdAt DESC")
    List<ReportAggregate> findByTenantAndCreatedBy(@Param("tenantId") TenantId tenantId,
                                                  @Param("userId") Integer userId);
    
    /**
     * Count reports created by user
     */
    @Query("SELECT COUNT(r) FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.createdBy = :userId")
    long countByTenantAndCreatedBy(@Param("tenantId") TenantId tenantId,
                                 @Param("userId") Integer userId);
    
    // ========== Validation Queries ==========
    
    /**
     * Check if similar report exists
     */
       @Query("SELECT COUNT(r) > 0 FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
              "AND r.reportType = :reportType AND r.startDate = :startDate " +
              "AND r.endDate = :endDate AND r.status IN ('GENERATING', 'COMPLETED')")
       boolean existsSimilarReport(@Param("tenantId") TenantId tenantId,
                            @Param("reportType") ReportType reportType,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);
       @Query("SELECT COUNT(r) > 0 FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
       "AND r.reportType = :reportType AND r.startDate = :startDate " +
       "AND r.endDate = :endDate AND r.status = 'GENERATING'")
       boolean existsGeneratingReport(@Param("tenantId") TenantId tenantId,
                                   @Param("reportType") ReportType reportType,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Find reports with duplicate names in tenant
     */
    @Query("SELECT r FROM ReportAggregate r WHERE r.tenantId = :tenantId " +
           "AND r.reportName = :reportName AND r.reportId != :excludeId")
    List<ReportAggregate> findDuplicateNames(@Param("tenantId") TenantId tenantId,
                                           @Param("reportName") String reportName,
                                           @Param("excludeId") Integer excludeId);
}