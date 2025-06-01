// backend/src/main/java/org/example/backend/application/dto/UpdateFixedAssetCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Command for updating fixed assets
 */
@Data
@Builder
public class UpdateFixedAssetCommand {
    private String name;
    private String description;
    private String location;
    private Integer companyId;
    private Integer departmentId;
}