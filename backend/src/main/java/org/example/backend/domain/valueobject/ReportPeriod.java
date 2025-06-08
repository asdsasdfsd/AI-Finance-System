// backend/src/main/java/org/example/backend/domain/valueobject/ReportPeriod.java
package org.example.backend.domain.valueobject;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Report Period Value Object
 * 
 * Encapsulates report time period logic and validation
 */
@Embeddable
public class ReportPeriod {
    
    public enum PeriodType {
        MONTHLY("Monthly", "月度"),
        QUARTERLY("Quarterly", "季度"), 
        YEARLY("Yearly", "年度"),
        CUSTOM("Custom", "自定义");
        
        private final String displayName;
        private final String chineseName;
        
        PeriodType(String displayName, String chineseName) {
            this.displayName = displayName;
            this.chineseName = chineseName;
        }
        
        public String getDisplayName() { return displayName; }
        public String getChineseName() { return chineseName; }
    }
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private PeriodType type;
    
    // JPA requires default constructor
    protected ReportPeriod() {}
    
    private ReportPeriod(LocalDate startDate, LocalDate endDate, PeriodType type) {
        validateDates(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }
    
    /**
     * Create custom period
     */
    public static ReportPeriod of(LocalDate startDate, LocalDate endDate) {
        return new ReportPeriod(startDate, endDate, PeriodType.CUSTOM);
    }
    
    /**
     * Create monthly period
     */
    public static ReportPeriod monthly(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return new ReportPeriod(startDate, endDate, PeriodType.MONTHLY);
    }
    
    /**
     * Create quarterly period
     */
    public static ReportPeriod quarterly(int year, int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
        
        int startMonth = (quarter - 1) * 3 + 1;
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = startDate.plusMonths(2).withDayOfMonth(startDate.plusMonths(2).lengthOfMonth());
        return new ReportPeriod(startDate, endDate, PeriodType.QUARTERLY);
    }
    
    /**
     * Create yearly period
     */
    public static ReportPeriod yearly(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return new ReportPeriod(startDate, endDate, PeriodType.YEARLY);
    }
    
    /**
     * Get current month period
     */
    public static ReportPeriod currentMonth() {
        LocalDate now = LocalDate.now();
        return monthly(now.getYear(), now.getMonthValue());
    }
    
    /**
     * Get previous month period
     */
    public static ReportPeriod previousMonth() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        return monthly(lastMonth.getYear(), lastMonth.getMonthValue());
    }
    
    /**
     * Get period duration in days
     */
    public long getDurationInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    /**
     * Check if period contains specific date
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Get formatted period description
     */
    public String getDescription() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s (%s to %s)", 
                           type.getDisplayName(), 
                           startDate.format(formatter), 
                           endDate.format(formatter));
    }
    
    /**
     * Check if this period overlaps with another
     */
    public boolean overlaps(ReportPeriod other) {
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }
    
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }
    
    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public PeriodType getType() { return type; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ReportPeriod that = (ReportPeriod) obj;
        return Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               Objects.equals(type, that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, type);
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}