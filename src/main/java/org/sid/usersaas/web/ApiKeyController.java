package org.sid.usersaas.web;

import lombok.AllArgsConstructor;
import org.sid.usersaas.dto.ApiKeyDTO;
import org.sid.usersaas.dto.NewApiKeyResponseDTO;
import org.sid.usersaas.service.AccountService;
import org.sid.usersaas.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/apikey")
public class ApiKeyController {

    private ApiKeyService apiKeyService;
	private AccountService accountService;

    /**
     * Tested and It's working in postman
     */
    @PostMapping("/{username}/keys")
    public ResponseEntity<NewApiKeyResponseDTO> generateApiKey(
            @PathVariable String username,
            @RequestBody Map<String, String> requestBody) {

        String authenticatedUsername = accountService.getCurrentAuthenticatedUsername();
        if (!authenticatedUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot generate key for another user.");
        }
        String name = requestBody.get("name"); // Get optional name from request body

        try {
            String plainTextKey = apiKeyService.generateAndSaveNewApiKey(authenticatedUsername, name, requestBody.get("description")); // Use authenticated ID
            // Return the plain text key ONCE wrapped in a DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(new NewApiKeyResponseDTO(plainTextKey));
        } catch (NullPointerException e) {
            // This shouldn't happen if authenticatedUserId is valid, but handle defensively
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Log the error server-side
            System.err.println("Error generating API key for user " + authenticatedUsername + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating API key");
        }
    }

    @GetMapping("/{username}/keys")
    public ResponseEntity<?> getApiKeys(@PathVariable String username) {
        String authenticatedUsername = accountService.getCurrentAuthenticatedUsername();
        if (!authenticatedUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot retrieve keys for another user.");
        }
        try {
            List<ApiKeyDTO> apiKeys = apiKeyService.getApiKeysForUser(authenticatedUsername);
            return ResponseEntity.ok(apiKeys);
        } catch (NullPointerException e) {
            // This shouldn't happen if authenticatedUserId is valid
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Log the error
            System.err.println("Error fetching API keys for user " + authenticatedUsername + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching API keys");
        }
    }

    @PostMapping("/{username}/keys/{apiKeyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Return 204 on success
    public ResponseEntity<String> revokeApiKey(
            @PathVariable String username,
            @PathVariable String apiKeyId) {

        // --- IMPORTANT SECURITY CHECK ---
        // Verify that the user ID in the path variable matches the authenticated user ID.
        // If using /me/keys, remove this check and get the ID directly from auth context.
        String authenticatedUsername = accountService.getCurrentAuthenticatedUsername();
        if (!authenticatedUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot revoke key for another user.");
        }
        try {
            apiKeyService.revokeApiKey(authenticatedUsername, apiKeyId);
            return ResponseEntity.ok("API key revoked successfully.");
        } catch (NullPointerException e) {
            // Return 404 if the key doesn't exist OR doesn't belong to this user
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Log the error
            System.err.println("Error revoking API key " + apiKeyId + " for user " + authenticatedUsername + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error revoking API key");
        }
    }

    @DeleteMapping("/{username}/keys/{apiKeyId}")
    public void deleteApiKey(
            @PathVariable String username,
            @PathVariable String apiKeyId) {

        // --- IMPORTANT SECURITY CHECK ---
        // Verify that the user ID in the path variable matches the authenticated user ID.
        // If using /me/keys, remove this check and get the ID directly from auth context.
        String authenticatedUsername = accountService.getCurrentAuthenticatedUsername();
        if (!authenticatedUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot revoke key for another user.");
        }
        // --- DELETE API KEY ---
        try {
            apiKeyService.deleteApiKey(authenticatedUsername, apiKeyId);
        } catch (NullPointerException e) {
            // Return 404 if the key doesn't exist OR doesn't belong to this user
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Log the error
            System.err.println("Error deleting API key " + apiKeyId + " for user " + authenticatedUsername + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting API key");
        }
    }
}
