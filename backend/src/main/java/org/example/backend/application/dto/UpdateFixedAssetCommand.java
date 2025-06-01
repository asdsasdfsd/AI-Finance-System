// backend/src/main/java/org/example/backend/application/dto/UpdateFixedAssetCommand.java
package org.example.backend.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFixedAssetCommand {
    private String name;
    private String description;
    private String location;
    private Integer companyId;
    private Integer departmentId;
}