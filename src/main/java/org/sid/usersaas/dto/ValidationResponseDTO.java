package org.sid.usersaas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ValidationResponseDTO {
    private String userId;
    private String apiKeyId;
}
