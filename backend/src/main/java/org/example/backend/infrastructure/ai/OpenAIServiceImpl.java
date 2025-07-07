package org.example.backend.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.example.backend.infrastructure.ai.dto.*;

import java.util.List;

@Slf4j
@Service("openaiService")
@RequiredArgsConstructor
public class OpenAIServiceImpl implements AIService {

    private final OpenAIClient client;

    @Override
    public AIClassificationResult classifyTransaction(String description, Double amount, String currency) {
        String prompt = "请根据描述和金额分类此交易：" + description + "，金额：" + amount + " " + currency;
        String response = client.callChatCompletion(prompt);

        return AIClassificationResult.builder()
                .suggestedCategory(response)
                .confidence(0.95)
                .reason("由AI自动分类")
                .needsManualReview(false)
                .build();
    }

    @Override
    public AIQuestionAnswerResult answerFinancialQuestion(String question, String context, Integer companyId) {
        String prompt = "财务背景信息如下：" + context + "\n请回答问题：" + question;
        String response = client.callChatCompletion(prompt);

        return AIQuestionAnswerResult.builder()
                .answer(response)
                .confidence("HIGH")
                .hasNumericData(true)
                .build();
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            client.callChatCompletion("你还在吗？");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public AIAnomalyDetectionResult detectAnomalousTransaction(AITransactionData data) {
        String prompt = "请判断这笔交易是否异常：" + data.toString();
        String reply = client.callChatCompletion(prompt);

        return AIAnomalyDetectionResult.builder()
                .isAnomalous(reply.contains("异常"))
                .anomalyScore(0.85)
                .anomalyType("金额异常")
                .description(reply)
                .recommendations(List.of("请人工复核", "核对交易来源"))
                .build();
    }

    @Override
    public AIReportInsightResult generateReportInsights(String reportData, String reportType) {
        String prompt = "请基于以下" + reportType + "类型的财务报表内容进行分析：\n" + reportData;

        try {
            String reply = client.callChatCompletion(prompt);
            return AIReportInsightResult.builder()
                    .insightSummary(reply)
                    .keyFindings(List.of("支出集中在市场部门", "收入同比增长 20%"))
                    .confidenceLevel("MEDIUM")
                    .build();
        } catch (Exception e) {
            log.error("报表洞察失败：{}", e.getMessage());
            return AIReportInsightResult.builder()
                    .insightSummary("AI 分析失败，请稍后重试。")
                    .confidenceLevel("LOW")
                    .build();
        }
    }

}

