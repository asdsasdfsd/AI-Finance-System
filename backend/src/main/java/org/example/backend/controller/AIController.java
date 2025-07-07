package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.service.AIApplicationService;
import org.example.backend.dto.QuestionRequest;
import org.example.backend.dto.ReportRequest;
import org.example.backend.infrastructure.ai.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIApplicationService aiApplicationService;

    // 🧠 智能交易分类
    @PostMapping("/classify")
    public ResponseEntity<AIClassificationResult> classifyTransaction(@RequestBody AITransactionData data) {
        AIClassificationResult result = aiApplicationService
                .enhanceTransactionCreation(data.toCreateTransactionCommand())
                .getAiClassification();
        return ResponseEntity.ok(result);
    }

    // 💬 财务智能问答
    @PostMapping("/ask")
    public ResponseEntity<AIQuestionAnswerResult> askQuestion(@RequestBody QuestionRequest request) {
        return ResponseEntity.ok(
                aiApplicationService.askFinancialQuestion(request.toCommand())
        );
    }

    // ⚠️ 异常交易检测
    @PostMapping("/detect")
    public ResponseEntity<AIAnomalyDetectionResult> detectAnomaly(@RequestBody AITransactionData data) {
        return ResponseEntity.ok(
                aiApplicationService.detectAnomalousTransaction(data).get(0) // 简化：取第一个结果
        );
    }

    // 📈 报表AI洞察（可选）
    @PostMapping("/report")
    public ResponseEntity<AIReportInsightResult> reportInsight(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(
                aiApplicationService.generateReportInsights(request.getReportData(), request.getReportType())
        );
    }
}

