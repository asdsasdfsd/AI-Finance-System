// backend/src/main/java/org/example/backend/domain/valueobject/ReportStatus.java
package org.example.backend.domain.valueobject;

/**
 * Report Status Enumeration
 * 
 * Represents the current state of report generation
 */
public enum ReportStatus {
    GENERATING("Generating", "生成中"),
    COMPLETED("Completed", "已完成"),
    FAILED("Failed", "失败"),
    ARCHIVED("Archived", "已归档");
    
    private final String displayName;
    private final String chineseName;
    
    ReportStatus(String displayName, String chineseName) {
        this.displayName = displayName;
        this.chineseName = chineseName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    /**
     * Check if report can be downloaded in this status
     */
    public boolean canDownload() {
        return this == COMPLETED || this == ARCHIVED;
    }
    
    /**
     * Check if report can be modified in this status
     */
    public boolean canModify() {
        return this != GENERATING;
    }
    
    /**
     * Check if this is a final status
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == ARCHIVED;
    }
}

