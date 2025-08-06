package org.example.backend.infrastructure.ai;

import org.example.backend.application.dto.*;

public interface AIService {

    /**
     * 智能交易分类
     * @param description 交易描述
     * @param amount 金额
     * @param currency 币种（如 CNY, USD）
     * @return 分类结果
     */
    AIClassificationResult classifyTransaction(String description, Double amount, String currency);

    /**
     * 财务问答
     * @param question 用户问题
     * @param context 财务数据上下文
     * @param companyId 公司ID
     * @return AI回答结果
     */
    AIQuestionAnswerResult answerFinancialQuestion(String question, String context, Integer companyId);

    /**
     * 异常交易检测
     * @param data 单笔交易数据
     * @return 异常分析结果
     */
    AIAnomalyDetectionResult detectAnomalousTransaction(AITransactionData data);

    /**
     * 报表智能洞察
     * @param reportData 报表原始内容
     * @param reportType 报表类型，如 Income, BalanceSheet
     * @return 洞察结果
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
     * 获取服务提供商名称
     * @return 如 OpenAI、LocalAI 等
     */
    String getProviderName();

    /**
     * 财务数据智能分析
     * @param command 分析命令对象，包含公司、时间区间、原始数据等
     * @return 智能分析结果
     */
    FinancialAnalysisDTO analyzeFinancialData(FinancialAnalysisCommand command);

    /**
     * 获取AI智能推荐
     * @param command 推荐命令对象，包含场景、目标对象等
     * @return 推荐结果
     */
    AIRecommendationsDTO getRecommendations(RecommendationCommand command);
}
