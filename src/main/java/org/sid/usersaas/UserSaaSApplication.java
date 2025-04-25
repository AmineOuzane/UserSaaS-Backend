package org.sid.usersaas;

import org.sid.usersaas.service.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserSaaSApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSaaSApplication.class, args);
    }

    @Bean
    CommandLineRunner initRoles(AccountService accountService) {
        return args -> {
            try {
                accountService.addNewRole("USER");
            } catch (RuntimeException e) {
                System.out.println("USER role already exists");
            }

            try {
                accountService.addNewRole("ADMIN");
            } catch (RuntimeException e) {
                System.out.println("ADMIN role already exists");
            }
        };
    }

}
