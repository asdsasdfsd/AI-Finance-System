package org.example.backend.application.dto;

import lombok.Data;

@Data
public class FinancialQuestionCommand {
    private String question;     // 用户提出的财务问题
    private String context;      // 财务上下文数据（如资产负债表等），可选
    private Integer companyId;   // 公司 ID
}

