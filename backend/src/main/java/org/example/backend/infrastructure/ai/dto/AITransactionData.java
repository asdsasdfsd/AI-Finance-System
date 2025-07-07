package org.example.backend.infrastructure.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class AITransactionData {
    private String description;                // 交易描述
    private Double amount;                     // 金额
    private String currency;                   // 币种
    private String category;                   // 当前分类
    private LocalDate transactionDate;         // 日期
    private String transactionType;            // INCOME / EXPENSE
    private Integer companyId;                 // 公司ID
    private Map<String, Object> metadata;      // 其他元数据

    public CreateTransactionCommand toCreateTransactionCommand() {
        CreateTransactionCommand cmd = new CreateTransactionCommand();
        cmd.setDescription(this.description);
        cmd.setAmount(this.amount);
        cmd.setCurrency(this.currency);
        return cmd;
    }
}

