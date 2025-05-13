package org.sid.usersaas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class AppRole {
    @Id
    private String role;
    @ManyToOne
    @JoinColumn(name = "user_id") // This assumes you have a foreign key in AppRole pointing to AppUser
    private AppUser appUser;
}
