// backend/src/main/java/org/example/backend/domain/valueobject/ReportType.java
package org.example.backend.domain.valueobject;

/**
 * Report Type Enumeration
 * 
 * Defines the four standard financial reports to be generated
 */
public enum ReportType {
    BALANCE_SHEET("Balance Sheet", "资产负债表"),
    INCOME_STATEMENT("Income Statement", "损益表"), 
    INCOME_EXPENSE("Income and Expense Report", "收入与支出报表"),
    FINANCIAL_GROUPING("Financial Grouping Report", "财务项目分组");
    
    private final String displayName;
    private final String chineseName;
    
    ReportType(String displayName, String chineseName) {
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
     * Check if this report type supports AI analysis
     */
    public boolean supportsAIAnalysis() {
        // All reports can potentially support AI analysis in the future
        return true;
    }
    
    /**
     * Get default file name for this report type
     */
    public String getDefaultFileName(String startDate, String endDate) {
        String baseFileName = name().toLowerCase() + "_" + startDate + "_to_" + endDate;
        return baseFileName.replace("-", "");
    }
}

