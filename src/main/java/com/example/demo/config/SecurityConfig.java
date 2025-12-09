package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Désactive CSRF pour les API REST
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/citoyens/**").permitAll()  // Autorise TOUTES les routes citoyens
                        .requestMatchers("/api/auth/**").permitAll()      // Autorise les routes d'authentification
                        .requestMatchers("/**").permitAll()               // ✅ AJOUTEZ CETTE LIGNE - Autorise tout temporairement
                        .anyRequest().permitAll()                         // ✅ AJOUTEZ CETTE LIGNE - Permet toutes les requêtes
                );

        return http.build();
    }
}