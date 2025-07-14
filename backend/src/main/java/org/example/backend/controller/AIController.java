package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.application.dto.*;
import org.example.backend.application.service.AIApplicationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIApplicationService aiApplicationService;

    /**
     * 增强交易（分类+异常检测）
     */
    @PostMapping("/enhance-transaction")
    public EnhancedTransactionDTO enhanceTransaction(@RequestBody CreateTransactionCommand command) {
        return aiApplicationService.enhanceTransactionCreation(command);
    }

    /**
     * 财务智能问答
     */
    @PostMapping("/ask-financial-question")
    public FinancialQuestionAnswerDTO askFinancialQuestion(@RequestBody FinancialQuestionCommand command) {
        return aiApplicationService.askFinancialQuestion(command);
    }

    /**
     * 获取分类建议（静态模拟）
     */
    @PostMapping("/category-suggestions")
    public List<CategorySuggestionDTO> getCategorySuggestions(@RequestBody CategorySuggestionCommand command) {
        return aiApplicationService.getTransactionCategorySuggestions(command);
    }

    /**
     * 批量异常交易检测
     */
    @GetMapping("/detect-anomalies")
    public List<AnomalousTransactionDTO> detectAnomalousTransactions(
            @RequestParam Integer companyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        DateRange dateRange = new DateRange(LocalDate.parse(startDate), LocalDate.parse(endDate));
        return aiApplicationService.detectAnomalousTransactions(companyId, dateRange);
    }

    /**
     * 智能生成财务报表洞察
     */
    @GetMapping("/report-insights")
    public String generateReportInsights(
            @RequestParam String reportData,
            @RequestParam String reportType) {
        return aiApplicationService.generateReportInsights(reportData, reportType);
    }
}
