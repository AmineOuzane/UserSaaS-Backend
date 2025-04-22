package org.sid.usersaas.web;

import lombok.AllArgsConstructor;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {
    private final AccountService accountService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/registerUser")
    public AppUser registerUser(@RequestBody AppUser appUser) {
        log.info("Registering new client: {}", appUser);
        return accountService.registerUser(appUser);
    }
}
