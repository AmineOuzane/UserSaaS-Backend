package org.sid.usersaas.service;

import org.sid.usersaas.dto.RegistrationUserDTO;
import org.sid.usersaas.entities.AppRole;
import org.sid.usersaas.entities.AppUser;

import java.math.BigDecimal;

public interface AccountService {
    AppUser registerUser(RegistrationUserDTO registrationUserDTO);
    AppRole addNewRole(String role);
    void addRoleToUser(String username, String role);
    void removeRoleFromUser(String username, String role);
    AppUser loadUserByUsername(String username);
    boolean deleteMyAccount(String username);
    boolean deactivateMyAccount(String username);
    String getCurrentAuthenticatedUsername();

}
