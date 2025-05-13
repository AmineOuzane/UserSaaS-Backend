package org.sid.usersaas.service;

import org.sid.usersaas.dto.ApiKeyDTO;
import org.sid.usersaas.dto.ValidationResponseDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ApiKeyService {

    String generateAndSaveNewApiKey(String userId, String name, String description);
    Optional<ValidationResponseDTO> validateApiKey(String plainTextApiKey);
    void updateLastUsed(Instant lastUsedAt);
    void revokeApiKey(String username, String apiKeyId);
    void deleteApiKey(String username, String apiKeyId);
    List<ApiKeyDTO> getApiKeysForUser(String username);
}
