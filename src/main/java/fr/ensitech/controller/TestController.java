package fr.ensitech.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de test pour vérifier que l'application fonctionne
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Application fonctionne correctement");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("vulnerable", "http://localhost:8080/vulnerable/users");
        endpoints.put("secure", "http://localhost:8080/secure/users");
        endpoints.put("h2-console", "http://localhost:8080/h2-console");
        
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("project", "Broken Access Control Demo");
        info.put("purpose", "Démonstration OWASP A01:2021");
        info.put("users", Map.of(
            "user", "user@example.com / password123",
            "admin", "admin@example.com / admin123",
            "alice", "alice@example.com / alice123"
        ));
        
        return ResponseEntity.ok(info);
    }
}
