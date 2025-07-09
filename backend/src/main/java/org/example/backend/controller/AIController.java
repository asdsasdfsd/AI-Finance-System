// backend/src/main/java/org/example/backend/controller/AIController.java
package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.service.AIApplicationService;
import org.example.backend.dto.QuestionRequest;
import org.example.backend.dto.ReportRequest;
import org.example.backend.infrastructure.ai.dto.AITransactionData;
import org.example.backend.infrastructure.ai.dto.AIClassificationResult;
import org.example.backend.infrastructure.ai.dto.AIQuestionAnswerResult;
import org.example.backend.infrastructure.ai.dto.AIAnomalyDetectionResult;
import org.example.backend.infrastructure.ai.dto.AIReportInsightResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Controller
 * 
 * REST API endpoints for AI-powered financial features
 * Provides intelligent transaction classification, anomaly detection,
 * financial Q&A, and report insights
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AIController {

    private final AIApplicationService aiApplicationService;

    /**
     * 🧠 Intelligent transaction classification
     * 
     * @param data Transaction data to classify
     * @return AI classification result
     */
    @PostMapping("/classify")
    public ResponseEntity<AIClassificationResult> classifyTransaction(@RequestBody AITransactionData data) {
        try {
            AIClassificationResult result = aiApplicationService
                    .enhanceTransactionCreation(data.toCreateTransactionCommand())
                    .getAiClassification();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Return error response if classification fails
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 💬 Financial intelligent Q&A
     * 
     * @param request Question request containing the financial question
     * @return AI answer result
     */
    @PostMapping("/ask")
    public ResponseEntity<AIQuestionAnswerResult> askQuestion(@RequestBody QuestionRequest request) {
        try {
            AIQuestionAnswerResult result = aiApplicationService
                    .askFinancialQuestion(request.toCommand());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Return error response if Q&A fails
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ⚠️ Anomalous transaction detection
     * 
     * @param data Transaction data to analyze for anomalies
     * @return AI anomaly detection result
     */
    @PostMapping("/detect")
    public ResponseEntity<AIAnomalyDetectionResult> detectAnomaly(@RequestBody AITransactionData data) {
        try {
            List<AIAnomalyDetectionResult> results = aiApplicationService
                    .detectAnomalousTransaction(data);
            
            if (results.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            // Return first result (simplified approach)
            return ResponseEntity.ok(results.get(0));
        } catch (Exception e) {
            // Return error response if detection fails
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 📈 Report AI insights (optional feature)
     * 
     * @param request Report request containing data and type
     * @return AI report insight result
     */
    @PostMapping("/report")
    public ResponseEntity<AIReportInsightResult> reportInsight(@RequestBody ReportRequest request) {
        try {
            AIReportInsightResult result = aiApplicationService
                    .generateReportInsights(request.getReportData(), request.getReportType());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Return error response if insights generation fails
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint for AI services
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI services are running");
    }
}