// backend/src/main/java/org/example/backend/application/dto/ReportDTO.java
package org.example.backend.application.dto;

import org.example.backend.domain.valueobject.ReportType;
import org.example.backend.domain.valueobject.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Report Data Transfer Object
 * 
 * Used for transferring report data between layers
 */
@Data
@Builder
public class ReportDTO {
    private Integer reportId;
    private String reportName;
    private ReportType reportType;
    private ReportStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String filePath;
    private String fileFormat;
    private Long fileSize;
    private String fileSizeFormatted;
    private Boolean aiAnalysisEnabled;
    private String aiAnalysisStatus;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
    private String periodDescription;
    
    // Computed properties
    public boolean isDownloadable() {
        return status != null && status.canDownload() && filePath != null;
    }
    
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == ReportStatus.FAILED;
    }
    
    public boolean isGenerating() {
        return status == ReportStatus.GENERATING;
    }
}

