package org.sid.usersaas.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.sid.usersaas.dto.RegistrationUserDTO;
import org.sid.usersaas.entities.AppRole;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.entities.CreditTransaction;
import org.sid.usersaas.entities.UsageRecord;
import org.sid.usersaas.enums.TransactionSource;
import org.sid.usersaas.repository.AppRoleRepository;
import org.sid.usersaas.repository.AppUserRepository;
import org.sid.usersaas.repository.UsageRecordRepository;
import org.sid.usersaas.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private  AppUserRepository appUserRepository;
    private  AppRoleRepository appRoleRepository;
    private  PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

    // Define the claim name used in the JWT
    private static final String USER_ID_CLAIM = "user_id"; // Must match the key used in Step 1

    @Override
    public AppUser registerUser(RegistrationUserDTO registrationUserDTO) {


        String hashedPassword = passwordEncoder.encode(registrationUserDTO.getPassword());
        AppUser newUser = AppUser.builder()
                .userId(UUID.randomUUID().toString())
                .username(registrationUserDTO.getUsername())
                .password(hashedPassword)
                .email(registrationUserDTO.getEmail())
                .roles(new ArrayList<>())
                .phone(registrationUserDTO.getPhone())
                .isActive(true)
                .companyName(registrationUserDTO.getCompanyName())
                .walletBalance(BigDecimal.valueOf(15)) // Default wallet balance
                .build();

        // First save the user
        AppUser savedUser = appUserRepository.save(newUser);

        // Then assign role
        addRoleToUser(savedUser.getUsername(), "USER");

        return savedUser;
    }

    @Override
    public AppRole addNewRole(String role) {
	appRoleRepository.findById(role).ifPresent(r -> {
            throw new RuntimeException("Role already exists");
        });
        AppRole appRole = AppRole.builder()
                .role(role)
                .build();
        return appRoleRepository.save(appRole);
    }

    @Override
    public void addRoleToUser(String username, String role) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        AppRole appRole = appRoleRepository.findById(role).orElseThrow(() -> new RuntimeException("Role not found"));
        appUser.getRoles().add(appRole);
        appUserRepository.save(appUser); // Save within the transaction
    }

    @Override
    public void removeRoleFromUser(String username, String role) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        AppRole appRole = appRoleRepository.findById(role).orElseThrow(() -> new RuntimeException("Role not found"));
        appUser.getRoles().remove(appRole);
        appUserRepository.save(appUser); // Save within the transaction
    }

    @Override
    public AppUser loadUserByUsername(String username) {

        return appUserRepository.findByUsername(username);
    }

    @Override
    public boolean deleteMyAccount(String username) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(username)) {
            throw new RuntimeException("You can only delete your own account");
        }
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        appUserRepository.delete(appUser);
        return true;
    }

    @Override
    public boolean deactivateMyAccount(String username) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(username)) {
            throw new RuntimeException("You can only deactivate your own account");
        }
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        appUser.setActive(false); // Assuming 'disabled' is a boolean field in AppUser
        appUserRepository.save(appUser);
        return true;
    }


    @Override
    public String getCurrentAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated.");
        }

        // The authentication.getName() method typically returns the principal's name (username in your case)
        return authentication.getName();
    }

}
