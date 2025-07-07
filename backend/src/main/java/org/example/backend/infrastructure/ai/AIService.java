package org.example.backend.infrastructure.ai;

import org.example.backend.infrastructure.ai.dto.*;

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
     * 服务健康检查
     * @return true 表示服务可用
     */
    boolean isServiceAvailable();

    /**
     * 获取服务提供商名称
     * @return 如 OpenAI、LocalAI 等
     */
    String getProviderName();
}
