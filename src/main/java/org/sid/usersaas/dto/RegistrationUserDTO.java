package org.sid.usersaas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistrationUserDTO {

    private String username;

    // Use the name that matches the frontend input field's 'name' attribute or JSON key
    @JsonProperty("telephone") // Frontend sends 'telephone'
    private String phone; // Mapping to your backend field name 'phone'

    // Fix the casing mismatch using @JsonProperty
    @JsonProperty("companyName")
    private String companyName;

    private String email;

    private String password;

    // You might handle confirm password validation in the frontend or backend service
    // @NotNull(message = "Confirm Password is required")
    // private String confirmPassword; // If you validate confirm password here

    // Do NOT include sensitive fields like isActive, roles, walletBalance, version here
    // unless they are explicitly part of the registration API contract.
}
