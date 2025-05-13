package org.sid.usersaas.repository;

import org.sid.usersaas.entities.ApiKey;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.enums.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

    // NOTE: While feasible due to unique constraint, direct lookup by BCrypt hash
    // can have performance implications compared to other key structures.
    Optional<ApiKey> findByKey(String hashedKey);

    // Find all keys belonging to a specific user
     List<ApiKey> findByAppUserUserId(String userId);


    // Find a specific key for a specific user (useful for revocation)
//    Optional<ApiKey> findByIdAndUserId(Long id, Long userId);

    // Find keys for a user by status
    List<ApiKey> findByAppUserAndStatus(AppUser appUser, ApiKeyStatus status);

    List<ApiKey> findByAppUser(AppUser appUser);
}
