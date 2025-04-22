package org.sid.usersaas.security;

import lombok.AllArgsConstructor;
import org.sid.usersaas.service.serviceImpl.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
// @EnableGlobalMethodSecurity(prePostEnabled = true) // Uncomment if you want to use method-level security
public class SecurityConfig {

    private UserDetailServiceImpl userDetailsServiceImpl;
    @Bean
    PasswordEncoder passwordEncoder() {
        // Algorithm de Hashage plus performant que MD5
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(httpSecurityCorsConfigurer -> // Enable CORS configuration
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)//  For simplicity; consider CSRF protection in production
        // form login still not tested thats why in comment
//        http.formLogin(form -> form
//                        .loginPage("/login") // Custom login page
//                        .permitAll() // Allow unauthenticated access to the login page
//                        .defaultSuccessUrl("/",true) // Redirect to / after successful login
//
//                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()// Allow OPTIONS requests for all endpoints (CORS preflight)
                        .requestMatchers(HttpMethod.POST, "/registerClient").permitAll() // Allow unauthenticated POST requests to /addClients (registration)
                        .anyRequest().permitAll() // All other requests does not require authentication
                );
        http.userDetailsService(userDetailsServiceImpl); // Set the custom UserDetailsService

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Allow your frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow credentials
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Register CORS filter for all paths
        return source;
    }
}
