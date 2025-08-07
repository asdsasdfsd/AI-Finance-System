package org.example.backend.application.dto;

import lombok.Data;

@Data
public class RecommendationCommand {
    private Integer companyId;
    private String scenario;         // 推荐场景，如"成本优化"、"投资决策"等
    private String targetObject;     // 针对的目标对象，如某部门、产品等
    private String data;             // 可选，附加数据
}

