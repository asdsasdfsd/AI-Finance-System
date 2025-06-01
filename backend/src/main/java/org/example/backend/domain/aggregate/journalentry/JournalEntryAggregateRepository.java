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
 * Journal Entry Aggregate Repository
 */
@Repository
public interface JournalEntryAggregateRepository extends JpaRepository<JournalEntryAggregate, Integer> {
    
    /**
     * Find journal entry by ID and tenant
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.entryId = :entryId AND j.tenantId = :tenantId")
    Optional<JournalEntryAggregate> findByIdAndTenant(@Param("entryId") Integer entryId, 
                                                     @Param("tenantId") TenantId tenantId);
    
    /**
     * Find journal entries by tenant
     */
    List<JournalEntryAggregate> findByTenantIdOrderByEntryDateDesc(TenantId tenantId);
    
    /**
     * Find journal entries by tenant and date range
     */
    @Query("SELECT j FROM JournalEntryAggregate j WHERE j.tenantId = :tenantId " +
           "AND j.entryDate BETWEEN :startDate AND :endDate ORDER BY j.entryDate DESC")
    List<JournalEntryAggregate> findByTenantIdAndDateRange(@Param("tenantId") TenantId tenantId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Find journal entries by tenant and status
     */
    List<JournalEntryAggregate> findByTenantIdAndStatus(TenantId tenantId, JournalEntryAggregate.EntryStatus status);
    
    /**
     * Find journal entries by tenant (alias method)
     */
    default List<JournalEntryAggregate> findByTenantId(TenantId tenantId) {
        return findByTenantIdOrderByEntryDateDesc(tenantId);
    }
}

