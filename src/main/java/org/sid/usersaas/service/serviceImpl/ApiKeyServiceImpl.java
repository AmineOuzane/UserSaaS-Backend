package org.sid.usersaas.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.sid.usersaas.dto.ApiKeyDTO;
import org.sid.usersaas.dto.ValidationResponseDTO;
import org.sid.usersaas.entities.ApiKey;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.enums.ApiKeyStatus;
import org.sid.usersaas.repository.ApiKeyRepository;
import org.sid.usersaas.repository.AppUserRepository;
import org.sid.usersaas.service.AccountService;
import org.sid.usersaas.service.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private ApiKeyRepository apiKeyRepository;
    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;
    private AccountService accountService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyServiceImpl.class);


    @Override
    public String generateAndSaveNewApiKey(String username, String name, String description) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if(appUser == null) {
            throw new RuntimeException("User not found");
        }

        if (appUser.getApiKeys().stream().filter(apiKey -> apiKey.getStatus() == ApiKeyStatus.ACTIVE).count() >= 5) {
            throw new RuntimeException("User has reached the maximum number of API keys");
        }

        byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits of entropy
        secureRandom.nextBytes(randomBytes);
        String plainTextKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        // Add a prefix to easily identify it as your API key format (optional but good practice)
        plainTextKey = "whso_" + plainTextKey; // Example: "whso_..."

        // 3. Hash the plain text key
        String hashedKey = passwordEncoder.encode(plainTextKey);
        ApiKey apiKey = new ApiKey();

        // --- CRITICAL FOR SAVING: Set the manual String ID ---
        apiKey.setId(UUID.randomUUID().toString()); // Generate and set a unique String ID (UUID)
        // ----------------------------------------------------

        // --- CRITICAL FOR SAVING & QUERYING: Associate API KEY WITH THE USER ---
        // You MUST set the AppUser object reference on the ApiKey entity before saving.
        apiKey.setAppUser(appUser); // <--- THIS LINE IS ESSENTIAL FOR THE RELATIONSHIP TO BE SAVED
        // --------------------------------------------------------------------

        apiKey.setName(name);
        apiKey.setDescription(description);
        apiKey.setKey(hashedKey); // Save the HASH
        apiKey.setCreatedAt(LocalDateTime.now()); // Use Instant
        apiKey.setStatus(ApiKeyStatus.ACTIVE); // Set initial status

        // 4. Save the entity to the database
        try {
            apiKeyRepository.save(apiKey); // The relationship (foreign key) is saved here
            logger.info("API key saved for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to save API key for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to save API key", e);
        }

        // 5. Return the plain text key (ONLY return it ONCE)
        return plainTextKey;
    }

    @Override
    public Optional<ValidationResponseDTO> validateApiKey(String plainTextApiKey) {
        if (plainTextApiKey == null || plainTextApiKey.trim().isEmpty()) {
            return Optional.empty(); // Cannot validate empty key
        }

        // --- IMPORTANT NOTE ON BCrypt VALIDATION ---
        // Due to how BCrypt hashing works (includes random salt), you cannot
        // efficiently search for a key record by hashing the incoming plaintext key
        // and looking for the matching stored hash using a simple DB lookup or index.
        // You MUST use passwordEncoder.matches(plainTextKey, storedHashedKey)
        // to perform the comparison.
        // This means you typically need a way to find the potential API key record
        // *before* you can call .matches().

        // Current approach using findByHashedKey:
        // This relies on the database index on `hashed_key` effectively allowing a lookup.
        // While seemingly intuitive, performance at very high scale might be a concern
        // compared to strategies involving a public, indexed part of the key.
        // For many applications, this approach is sufficient, but monitor performance.
        // Find the key record that might match based on the hash lookup.
        Optional<ApiKey> apiKeyOptional = apiKeyRepository.findByKey(passwordEncoder.encode(plainTextApiKey)); // Potential BCrypt lookup efficiency issue

        if (apiKeyOptional.isPresent()) {
            ApiKey apiKey = apiKeyOptional.get();

            // 2. Use passwordEncoder.matches to securely compare
            // This is the TRUE validation step with BCrypt
            if (passwordEncoder.matches(plainTextApiKey, apiKey.getKey()) && apiKey.getStatus()== ApiKeyStatus.ACTIVE) {

                // 3. Validation successful and key is active - Update last used time
                // Verify this method if it actually work
                updateLastUsed(Instant.now());
                // apiKeyRepository.save(apiKey); // No need to explicitly save if within @Transactional and entity is managed

                // 4. Return user and key info (get details from the related User entity)
                return Optional.of(new ValidationResponseDTO(
                        apiKey.getAppUser().getUserId(),
                        apiKey.getId()
                ));

            } else {
                logger.warn("API Key validation failed: Key found but not active or match failed. Key ID: {}, Status: {}", apiKey.getId(), apiKey.getStatus());
                return Optional.empty();
            }

        } else {
            logger.warn("API Key validation failed: Key not found in DB.");
            return Optional.empty();
        }
    }

    @Override
    public void revokeApiKey(String username, String apiKeyId) {
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKey -> {
            if (apiKey.getAppUser().getUsername().equals(username) && apiKey.getStatus() != ApiKeyStatus.REVOKED) {
                apiKey.setStatus(ApiKeyStatus.REVOKED);
                apiKeyRepository.save(apiKey);
            } else {
                throw new RuntimeException("API key does not belong to the user");
            }
        });
    }

    @Override
    public void deleteApiKey(String username, String apiKeyId) {
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKey -> {
            if (apiKey.getAppUser().getUsername().equals(username)) {
                apiKeyRepository.delete(apiKey);
            } else {
                throw new RuntimeException("API key does not belong to the user");
            }
        });
    }

    @Override
    public List<ApiKeyDTO> getApiKeysForUser(String username) {

        logger.info("Fetching API keys for user: {}", username);
        AppUser appUser = accountService.loadUserByUsername(username); // This should return the AppUser for AmineOuzane
        if (appUser == null) {
            logger.warn("Attempted to fetch keys for non-existent user: {}", username);
            return List.of();
        }
        logger.debug("User {} loaded for key fetching. AppUser ID: {}", username, appUser.getUserId()); // <-- Log the loaded AppUser ID
        // --- USE THE REPOSITORY METHOD TO FIND BY AppUser OBJECT ---
        List<ApiKey> apiKeys = apiKeyRepository.findByAppUser(appUser);

        if (apiKeys.isEmpty()) {
            logger.warn("No API keys found for user ID: {}", appUser.getUsername());
            return List.of(); // Return an empty list if no keys are found
        }
        return apiKeys.stream()
                .map(apiKey -> new ApiKeyDTO(
                        apiKey.getId(),
                        apiKey.getName(),
                        apiKey.getDescription(),
                        apiKey.getCreatedAt(),
                        apiKey.getLastUsedAt(),
                        apiKey.getStatus()
                ))
                .toList();
    }

    @Override
    public void updateLastUsed(Instant lastUsed) {
         Instant.now();
    }
}
