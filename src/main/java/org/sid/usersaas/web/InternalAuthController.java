package org.sid.usersaas.web;

import lombok.AllArgsConstructor;
import org.sid.usersaas.dto.ApiKeyValidationRequestDTO;
import org.sid.usersaas.dto.ValidationResponseDTO;
import org.sid.usersaas.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/auth")
public class InternalAuthController {

    	private ApiKeyService apiKeyService;

        @PostMapping("/validate-api-key")
        public ResponseEntity<ValidationResponseDTO> validateApiKey(@RequestBody ApiKeyValidationRequestDTO request) {
            Optional<ValidationResponseDTO> validationResult = apiKeyService.validateApiKey(request.getApiKey());

            // Key is valid and active, return user/key info
            // Key is invalid, not found, or not active
            // HTTP 401 Unauthorized
            return validationResult.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
}
