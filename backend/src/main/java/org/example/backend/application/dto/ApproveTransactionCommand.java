// backend/src/main/java/org/example/backend/application/dto/ApproveTransactionCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Command for approving transactions
 */
@Data
@Builder
public class ApproveTransactionCommand {
    private Integer companyId;
    private Integer approverUserId;
    private String approvalNote;
}