package org.sid.usersaas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sid.usersaas.enums.ApiKeyStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor @AllArgsConstructor
public class ApiKeyDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Instant lastUsedAt;
    private ApiKeyStatus status;
}
