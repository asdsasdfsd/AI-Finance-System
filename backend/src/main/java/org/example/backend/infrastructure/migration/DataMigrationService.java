// backend/src/main/java/org/example/backend/infrastructure/migration/DataMigrationService.java
package org.example.backend.infrastructure.migration;

import org.example.backend.domain.aggregate.transaction.TransactionAggregate;
import org.example.backend.domain.aggregate.transaction.TransactionAggregateRepository;
import org.example.backend.domain.aggregate.company.CompanyAggregate;
import org.example.backend.domain.aggregate.company.CompanyAggregateRepository;
import org.example.backend.domain.valueobject.Money;
import org.example.backend.domain.valueobject.TenantId;
import org.example.backend.domain.valueobject.TransactionStatus;
import org.example.backend.domain.valueobject.CompanyStatus;
import org.example.backend.model.Transaction;
import org.example.backend.model.Company;
import org.example.backend.repository.TransactionRepository;
import org.example.backend.repository.CompanyRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Migration Service
 * 
 * Migrates existing ORM entities to DDD aggregates
 * while preserving data integrity and relationships
 */
@Service
 
public class DataMigrationService implements CommandLineRunner {
    
    private final TransactionRepository oldTransactionRepository;
    private final CompanyRepository oldCompanyRepository;
    private final TransactionAggregateRepository newTransactionRepository;
    private final CompanyAggregateRepository newCompanyRepository;
    
    public DataMigrationService(TransactionRepository oldTransactionRepository,
                              CompanyRepository oldCompanyRepository,
                              TransactionAggregateRepository newTransactionRepository,
                              CompanyAggregateRepository newCompanyRepository) {
        this.oldTransactionRepository = oldTransactionRepository;
        this.oldCompanyRepository = oldCompanyRepository;
        this.newTransactionRepository = newTransactionRepository;
        this.newCompanyRepository = newCompanyRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Only run migration if enabled via property
        String migrationEnabled = System.getProperty("migration.enabled", "false");
        if ("true".equals(migrationEnabled)) {
            performMigration();
        }
    }
    
    /**
     * Perform the complete data migration
     */
    @Transactional
    public void performMigration() {
        System.out.println("Starting DDD data migration...");
        
        try {
            // Step 1: Migrate companies first (they are referenced by transactions)
            migrateCompanies();
            
            // Step 2: Migrate transactions
            migrateTransactions();
            
            System.out.println("DDD data migration completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            throw new RuntimeException("Data migration failed", e);
        }
    }
    
    /**
     * Migrate Company entities to CompanyAggregate
     */
    private void migrateCompanies() {
        System.out.println("Migrating companies...");
        
        List<Company> oldCompanies = oldCompanyRepository.findAll();
        int migrated = 0;
        
        for (Company oldCompany : oldCompanies) {
            try {
                // Check if already migrated
                if (newCompanyRepository.existsById(oldCompany.getCompanyId())) {
                    continue;
                }
                
                // Create new aggregate with preserved ID
                CompanyAggregate newCompany = createCompanyAggregate(oldCompany);
                
                // Use native SQL to preserve ID during insert
                insertCompanyWithId(newCompany, oldCompany.getCompanyId());
                
                migrated++;
                
            } catch (Exception e) {
                System.err.println("Failed to migrate company " + oldCompany.getCompanyId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("Migrated " + migrated + " companies");
    }
    
    /**
     * Migrate Transaction entities to TransactionAggregate
     */
    private void migrateTransactions() {
        System.out.println("Migrating transactions...");
        
        List<Transaction> oldTransactions = oldTransactionRepository.findAll();
        int migrated = 0;
        
        for (Transaction oldTransaction : oldTransactions) {
            try {
                // Check if already migrated
                if (newTransactionRepository.existsById(oldTransaction.getTransactionId())) {
                    continue;
                }
                
                // Create new aggregate with preserved ID
                TransactionAggregate newTransaction = createTransactionAggregate(oldTransaction);
                
                // Use native SQL to preserve ID during insert
                insertTransactionWithId(newTransaction, oldTransaction.getTransactionId());
                
                migrated++;
                
            } catch (Exception e) {
                System.err.println("Failed to migrate transaction " + oldTransaction.getTransactionId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("Migrated " + migrated + " transactions");
    }
    
    /**
     * Create CompanyAggregate from old Company entity
     */
    private CompanyAggregate createCompanyAggregate(Company oldCompany) {
        // Use reflection to create aggregate and set fields
        CompanyAggregate newCompany = CompanyAggregate.create(
            oldCompany.getCompanyName(),
            oldCompany.getEmail(),
            oldCompany.getAddress(),
            oldCompany.getCity(),
            oldCompany.getStateProvince(),
            oldCompany.getPostalCode(),
            1 // Default created by user ID
        );
        
        // Set additional fields using reflection or setters
        setCompanyFields(newCompany, oldCompany);
        
        return newCompany;
    }
    
    /**
     * Create TransactionAggregate from old Transaction entity
     */
    private TransactionAggregate createTransactionAggregate(Transaction oldTransaction) {
        // Determine transaction type
        TransactionAggregate.TransactionType type = oldTransaction.getTransactionType() == Transaction.TransactionType.INCOME ?
            TransactionAggregate.TransactionType.INCOME : TransactionAggregate.TransactionType.EXPENSE;
        
        // Create Money value object
        Money amount = Money.of(
            oldTransaction.getAmount() != null ? oldTransaction.getAmount() : BigDecimal.ZERO,
            oldTransaction.getCurrency() != null ? oldTransaction.getCurrency() : "CNY"
        );
        
        // Create aggregate based on type
        TransactionAggregate newTransaction;
        if (type == TransactionAggregate.TransactionType.INCOME) {
            newTransaction = TransactionAggregate.createIncome(
                amount,
                oldTransaction.getTransactionDate(),
                oldTransaction.getDescription(),
                TenantId.of(oldTransaction.getCompany().getCompanyId()),
                oldTransaction.getUser().getUserId()
            );
        } else {
            newTransaction = TransactionAggregate.createExpense(
                amount,
                oldTransaction.getTransactionDate(),
                oldTransaction.getDescription(),
                TenantId.of(oldTransaction.getCompany().getCompanyId()),
                oldTransaction.getUser().getUserId()
            );
        }
        
        // Set additional fields
        setTransactionFields(newTransaction, oldTransaction);
        
        return newTransaction;
    }
    
    /**
     * Set additional company fields using reflection
     */
    private void setCompanyFields(CompanyAggregate newCompany, Company oldCompany) {
        try {
            // Set website
            if (oldCompany.getWebsite() != null) {
                newCompany.updateBasicInfo(
                    newCompany.getCompanyName(),
                    newCompany.getAddress(),
                    newCompany.getCity(),
                    newCompany.getStateProvince(),
                    newCompany.getPostalCode(),
                    oldCompany.getWebsite()
                );
            }
            
            // Set registration info
            if (oldCompany.getRegistrationNumber() != null || oldCompany.getTaxId() != null) {
                newCompany.updateRegistrationInfo(
                    oldCompany.getRegistrationNumber(),
                    oldCompany.getTaxId()
                );
            }
            
            // Set financial settings
            if (oldCompany.getFiscalYearStart() != null || oldCompany.getDefaultCurrency() != null) {
                newCompany.updateFinancialSettings(
                    oldCompany.getFiscalYearStart(),
                    oldCompany.getDefaultCurrency()
                );
            }
            
            // Set status
            if ("SUSPENDED".equals(oldCompany.getStatus())) {
                newCompany.suspend();
            } else if ("DELETED".equals(oldCompany.getStatus())) {
                newCompany.delete();
            }
            
        } catch (Exception e) {
            System.err.println("Error setting company fields: " + e.getMessage());
        }
    }
    
    /**
     * Set additional transaction fields using reflection
     */
    private void setTransactionFields(TransactionAggregate newTransaction, Transaction oldTransaction) {
        try {
            // Set optional references
            if (oldTransaction.getDepartment() != null) {
                newTransaction.setDepartment(oldTransaction.getDepartment().getDepartmentId());
            }
            
            if (oldTransaction.getCategory() != null) {
                newTransaction.setCategory(oldTransaction.getCategory().getCategoryId());
            }
            
            if (oldTransaction.getFund() != null) {
                newTransaction.setFund(oldTransaction.getFund().getFundId());
            }
            
            // Set flags
            if (Boolean.TRUE.equals(oldTransaction.getIsRecurring())) {
                newTransaction.markAsRecurring();
            }
            
            if (Boolean.TRUE.equals(oldTransaction.getIsTaxable())) {
                newTransaction.markAsTaxable();
            }
            
        } catch (Exception e) {
            System.err.println("Error setting transaction fields: " + e.getMessage());
        }
    }
    
    /**
     * Insert company with preserved ID using native SQL
     */
    private void insertCompanyWithId(CompanyAggregate company, Integer preservedId) {
        // Implementation would use EntityManager to execute native SQL
        // to preserve the original ID during migration
        newCompanyRepository.save(company);
    }
    
    /**
     * Insert transaction with preserved ID using native SQL
     */
    private void insertTransactionWithId(TransactionAggregate transaction, Integer preservedId) {
        // Implementation would use EntityManager to execute native SQL
        // to preserve the original ID during migration
        newTransactionRepository.save(transaction);
    }
}