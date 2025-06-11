// backend/src/main/java/org/example/backend/infrastructure/report/ReportFileManager.java
package org.example.backend.infrastructure.report;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Report File Manager
 * 
 * Handles file system operations for report files
 */
@Component
public class ReportFileManager {
    
    @Value("${app.reports.storage.path:./reports}")
    private String reportsStoragePath;
    
    /**
     * Save workbook to file system
     */
    public String saveWorkbook(Workbook workbook, String fileName) throws IOException {
        // Ensure reports directory exists
        ensureDirectoryExists();
        
        String filePath = Paths.get(reportsStoragePath, fileName).toString();
        
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }
        
        return filePath;
    }
    
    /**
     * Get file size in bytes
     */
    public Long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) ? Files.size(path) : null;
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Delete file
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Get file name from path
     */
    public String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }
    
    /**
     * Ensure reports directory exists
     */
    private void ensureDirectoryExists() throws IOException {
        Path reportsDir = Paths.get(reportsStoragePath);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }
    }
}

