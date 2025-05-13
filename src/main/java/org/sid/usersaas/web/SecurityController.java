package org.sid.usersaas.web;

import lombok.AllArgsConstructor;
import org.sid.usersaas.entities.AppUser;
import org.sid.usersaas.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class SecurityController {

	private AccountService accountService;
	private AuthenticationManager authenticationManager;
	private JwtEncoder jwtEncoder;
	private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

	/**
		Endpoint receive Http Request with username & password
		Authenticate the user
		Generate JWT token for the authenticated user
			Header Content-Type: application/x-www-form-urlencoded
			Body username=Amine&password=111111
	 */

	@PostMapping("/login")
	public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
		log.info("Authenticating user: {}", username);

		// Authenticate the user with userDetailsService et passwordEncoder et return objet type authentication for the authenticated user
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password)
		);
		log.info("User authenticated: {}", authentication.getName());

		// --- START: Get AppUser.userId by reloading the user ---
		// Get the username from the authenticated principal (standard User object)
		String authenticatedUsername = authentication.getName(); // Same as username parameter

		// Load the AppUser object using the username
		AppUser appUser = accountService.loadUserByUsername(authenticatedUsername);
		// You might want error handling here just in case, though it should exist after successful auth
		if (appUser == null) {
			throw new IllegalStateException("Authenticated user not found in service after authentication.");
		}
		String userId = appUser.getUserId(); // <-- Get the String userId from the reloaded AppUser
		// --- END: Get AppUser.userId by reloading the user ---


		// Generate JWT token for the authenticated user
		Instant instant = Instant.now();
		// Extract roles from the authentication and separate them with a space
		String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

		// Create JWT claims set
		JwtClaimsSet jwtClaimsSet= JwtClaimsSet.builder()
				.issuedAt(instant)
				.expiresAt(instant.plus(10, ChronoUnit.MINUTES))
				.subject(username)
				.claim("scope",scope)
				.claim("userId", userId)
				.build();

		// Create JWT header
		JwtEncoderParameters jwtEncoderParamters =
				JwtEncoderParameters.from(
						// Use same ALGO HS512 in the jwtDecoder
						JwsHeader.with(MacAlgorithm.HS512).build(),
						jwtClaimsSet
				);
		// Encode the JWT token
		String jwt = jwtEncoder.encode(jwtEncoderParamters).getTokenValue();

		return Map.of("access_token", jwt);
	}


	@GetMapping("/profil")
	public Authentication authentication(Authentication authentication) {
		return authentication;
	}
}