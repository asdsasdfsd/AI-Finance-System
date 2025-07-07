package org.example.backend.dto;

import lombok.Data;
import org.example.backend.application.dto.FinancialQuestionCommand;

@Data
public class QuestionRequest {
    private String question;
    private Integer companyId;

    public FinancialQuestionCommand toCommand() {
        FinancialQuestionCommand cmd = new FinancialQuestionCommand();
        cmd.setQuestion(this.question);
        cmd.setCompanyId(this.companyId);
        return cmd;
    }
}
