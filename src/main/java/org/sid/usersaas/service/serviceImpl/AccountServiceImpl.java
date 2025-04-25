package org.sid.usersaas.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.sid.usersaas.entities.AppRole;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.repository.AppRoleRepository;
import org.sid.usersaas.repository.AppUserRepository;
import org.sid.usersaas.service.AccountService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUser registerUser(AppUser appUser) {

        String hashedPassword = passwordEncoder.encode(appUser.getPassword());
        AppUser newUser = AppUser.builder()
                .userId(UUID.randomUUID().toString())
                .username(appUser.getUsername())
                .password(hashedPassword)
                .email(appUser.getEmail())
                .roles(new ArrayList<>())
                .phone(appUser.getPhone())
                .isActive(true)
                .companyName(appUser.getCompanyName())
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


}
