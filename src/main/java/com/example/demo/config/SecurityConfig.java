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
                // CSRF désactivé (formulaire + session)
                .csrf(csrf -> csrf.disable())

                //  Autorisations
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/logout",
                                "/oauth2/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().permitAll() //  Laisse passer, HttpSession gère
                )

                // OAuth2 uniquement comme fournisseur d'identité
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/oauth2/success", true)
                )

                //  Désactiver le login Spring Security
                .formLogin(form -> form.disable())

                //  Désactiver remember-me
                .rememberMe(remember -> remember.disable());

        return http.build();
    }
}
