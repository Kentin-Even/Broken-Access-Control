package fr.ensitech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security
 * 
 * Points clés :
 * - @EnableMethodSecurity : Active les annotations @PreAuthorize
 * - CSRF désactivé pour API REST
 * - Session stateless (pour JWT dans une vraie app)
 * - Endpoints /vulnerable/** accessibles sans auth (pour démonstration)
 * - Endpoints /secure/** nécessitent authentification
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour API REST
            .csrf(csrf -> csrf.disable())
            
            // Configuration des autorisations par endpoints
            .authorizeHttpRequests(auth -> auth
                // Console H2 (développement uniquement)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Endpoints de test et documentation
                .requestMatchers("/", "/test/**", "/api-docs/**").permitAll()
                
                // ⚠️ Endpoints VULNÉRABLES : accessibles sans auth (pour démonstration)
                .requestMatchers("/vulnerable/**").permitAll()
                
                // ✅ Endpoints SÉCURISÉS : authentification requise
                .requestMatchers("/secure/**").authenticated()
                
                // Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Configuration HTTP Basic Auth (simplifié pour démo)
            .httpBasic(basic -> {})
            
            // Session stateless (préparé pour JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Permettre les frames (pour console H2)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
