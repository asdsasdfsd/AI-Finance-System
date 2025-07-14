package org.example.backend.infrastructure.ai;

import org.example.backend.application.dto.*;

public interface AIService {

    /**
     * 分类交易（通过 prompt）
     */
    AIClassificationResult classifyTransaction(String prompt);

    /**
     * 财务问答（通过 prompt）
     */
    AIQuestionAnswerResult answerFinancialQuestion(String prompt);

    /**
     * 异常检测
     */
    AIAnomalyDetectionResult detectAnomalousTransaction(AITransactionData transactionData);

    /**
     * 生成报表洞察
     */
    AIReportInsightResult generateReportInsights(String reportData, String reportType);

    /**
     * 通用 Prompt 调用
     */
    String call(String prompt);

    /**
     * 健康检查
     */
    boolean isServiceAvailable();

    /**
     * 获取服务提供者名称
     */
    String getProviderName();
} 
