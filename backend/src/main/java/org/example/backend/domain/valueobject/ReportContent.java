// backend/src/main/java/org/example/backend/domain/valueobject/ReportContent.java
package org.example.backend.domain.valueobject;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Report Content Value Object - DDD Compliant
 * 
 * Represents the content data of a report as an immutable value object
 * This is part of the Report Aggregate, not a separate entity
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportContent {
    
    @Lob
    @Column(name = "content_data")
    private String data;
    
    @Column(name = "content_format")
    private String format;
    
    @Column(name = "content_size")
    private Long size;
    
    @Column(name = "content_hash")
    private String hash;
    
    /**
     * Create new report content
     */
    public static ReportContent create(String data, String format) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Report content data cannot be empty");
        }
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Report content format cannot be empty");
        }
        
        return new ReportContent(
            data,
            format,
            (long) data.length(),
            generateHash(data)
        );
    }
    
    /**
     * Check if content is available for viewing
     */
    public boolean isViewable() {
        return data != null && !data.trim().isEmpty();
    }
    
    /**
     * Check if content is suitable for AI analysis
     */
    public boolean isSuitableForAI() {
        return isViewable() && 
               ("JSON".equals(format) || "STRUCTURED_JSON".equals(format)) &&
               data.length() > 10; // Minimum meaningful content
    }
    
    /**
     * Get content summary for display
     */
    public String getSummary() {
        if (data == null || data.length() < 100) {
            return data;
        }
        return data.substring(0, 97) + "...";
    }
    
    /**
     * Get formatted size string
     */
    public String getFormattedSize() {
        if (size == null) return "Unknown";
        
        double sizeValue = size.doubleValue();
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (sizeValue >= 1024 && unitIndex < units.length - 1) {
            sizeValue /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", sizeValue, units[unitIndex]);
    }
    
    /**
     * Generate simple hash for content integrity
     */
    private static String generateHash(String data) {
        return String.valueOf(data.hashCode());
    }
    
    /**
     * Value object equality based on content
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ReportContent)) return false;
        
        ReportContent other = (ReportContent) obj;
        return data != null ? data.equals(other.data) : other.data == null;
    }
    
    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}