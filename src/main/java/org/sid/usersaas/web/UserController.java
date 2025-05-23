package org.sid.usersaas.web;

import lombok.AllArgsConstructor;
import org.sid.usersaas.dto.RegistrationUserDTO;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.repository.AppUserRepository;
import org.sid.usersaas.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.DescriptorKey;
import java.io.IOException;

@RestController
@AllArgsConstructor
public class UserController {
    private  AccountService accountService;
    private AppUserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/registerUser")
    public AppUser registerUser(@RequestBody RegistrationUserDTO registrationUserDTO) {
        log.info("Registering new client: {}", registrationUserDTO);
        return accountService.registerUser(registrationUserDTO);
    }

    @GetMapping("/user/{username}")
    public AppUser getUserByUsername(@PathVariable String username) {
        log.info("Fetching user by username: {}", username);
        return accountService.loadUserByUsername(username);
    }

    @DeleteMapping("/deleteMyAccount")
    public ResponseEntity<String> deleteMyAccount(Authentication authentication) {
        // Spring Security injects Authentication if the JWT filter ran successfully
        if (authentication == null || !authentication.isAuthenticated()) {
            // This shouldn't happen if security is configured correctly, but good practice
            log.warn("Attempt to delete account without proper authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }

        String authenticatedUsername = authentication.getName(); // Get username SECURELY from the token
        log.info("Request received to delete own account for user: {}", authenticatedUsername);

        boolean deleted = accountService.deleteMyAccount(authenticatedUsername); // Use the authenticated username

        if (deleted) {
            return ResponseEntity.ok("Account deleted successfully.");
        } else {
            // This might mean an internal issue if the authenticated user couldn't be found
            log.error("Could not delete account for authenticated user: {}", authenticatedUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete account.");
        }
    }

    @PostMapping("/deactivateMyAccount")
    public ResponseEntity<String> deactivateMyAccount(Authentication authentication) {
        // Spring Security injects Authentication if the JWT filter ran successfully
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempt to disable account without proper authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required.");
        }

        String authenticatedUsername = authentication.getName();
        log.info("Request received to disable own account for user: {}", authenticatedUsername);

        boolean deactivated = accountService.deactivateMyAccount(authenticatedUsername); // Use authenticated username

        if (deactivated) {
            return ResponseEntity.ok("Account disabled successfully.");
        } else {
            log.error("Could not disable account for authenticated user: {}", authenticatedUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to disable account.");
        }
    }

    @PostMapping("/upload-profile-picture/{userId}")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable String userId,
                                                       @RequestParam("file") MultipartFile file) {
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setProfilePicture(file.getBytes());
            userRepository.save(user);

            return ResponseEntity.ok("Profile picture uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload profile picture.");
        }
    }

}
