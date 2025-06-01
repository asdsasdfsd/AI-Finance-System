// backend/src/main/java/org/example/backend/domain/aggregate/journalentry/JournalEntryAggregateRepository.java
package org.example.backend.domain.aggregate.journalentry;

import org.example.backend.domain.valueobject.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Journal Entry Aggregate Repository - 修复版本
 */
@Repository
public interface JournalEntryAggregateRepository extends JpaRepository<JournalEntryAggregate, Integer> {
    
    /**
     * Find journal entry by ID and tenant
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.journalId = :entryId AND j.companyId = :companyId")
    Optional<JournalEntryAggregate> findByIdAndTenant(@Param("entryId") Integer entryId, 
                                                     @Param("companyId") Integer companyId);
    
    /**
     * Find journal entries by tenant
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.companyId = :companyId ORDER BY j.entryDate DESC")
    List<JournalEntryAggregate> findByTenantIdOrderByEntryDateDesc(@Param("companyId") Integer companyId);
    
    /**
     * Find journal entries by tenant and date range
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.companyId = :companyId " +
           "AND j.entryDate BETWEEN :startDate AND :endDate ORDER BY j.entryDate DESC")
    List<JournalEntryAggregate> findByTenantIdAndDateRange(@Param("companyId") Integer companyId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Find journal entries by tenant and status
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.companyId = :companyId AND j.status = :status")
    List<JournalEntryAggregate> findByTenantIdAndStatus(@Param("companyId") Integer companyId, 
                                                       @Param("status") JournalEntryAggregate.EntryStatus status);
    
    /**
     * Find journal entries by tenant (alias method)
     */
    default List<JournalEntryAggregate> findByTenantId(TenantId tenantId) {
        return findByTenantIdOrderByEntryDateDesc(tenantId.getValue());
    }
}