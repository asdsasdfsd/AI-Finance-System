// FinancialQuestionCommand.java
package org.example.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor 
@Builder
@NoArgsConstructor   

public class FinancialQuestionCommand {
    private String question;    // 用户提出的问题
    private Integer companyId;  // 公司 ID，供上下文构建使用
    private String language;    // 可选：问题语言（如中文/英文）
    private LocalDate startDate; // 可选：查询起始时间
    private LocalDate endDate;   // 可选：查询结束时间
}
