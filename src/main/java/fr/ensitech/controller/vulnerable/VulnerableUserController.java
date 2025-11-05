package fr.ensitech.controller.vulnerable;

import fr.ensitech.model.User;
import fr.ensitech.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ⚠️⚠️⚠️ CONTROLLER VULNÉRABLE - À DES FINS DE DÉMONSTRATION UNIQUEMENT ⚠️⚠️⚠️
 * 
 * Ce controller démontre plusieurs vulnérabilités Broken Access Control :
 * 1. Mass Assignment - Entité User directement exposée
 * 2. IDOR - Pas de vérification de propriété
 * 3. Missing Function Level Access Control - Pas de vérification de rôles
 * 4. Information Disclosure - Exposition de données sensibles
 * 
 * ❌ NE JAMAIS FAIRE CELA EN PRODUCTION ❌
 */
@RestController
@RequestMapping("/vulnerable/users")
@CrossOrigin(origins = "*")
public class VulnerableUserController {

    private final UserRepository userRepository;
    
    public VulnerableUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ❌ VULNÉRABILITÉ 1 : Mass Assignment
     * L'entité User complète est exposée, permettant de modifier TOUS les champs
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        // ⚠️ DANGER : Aucune vérification
        // Un attaquant peut envoyer n'importe quel champ : roles, accountBalance, etc.
        
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        user.setId(id);
        User savedUser = userRepository.save(user);
        
        return ResponseEntity.ok(savedUser);
    }

    /**
     * ❌ VULNÉRABILITÉ 2 : IDOR (Insecure Direct Object Reference)
     * N'importe quel utilisateur peut accéder aux données de n'importe quel autre utilisateur
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        // ⚠️ DANGER : Pas de vérification que l'utilisateur accède à ses propres données
        
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ❌ VULNÉRABILITÉ 3 : Missing Function Level Access Control
     * Endpoint "admin" accessible sans vérification de rôle
     */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        // ⚠️ DANGER : Pas de vérification de rôle ADMIN
        // N'importe qui peut lister tous les utilisateurs avec leurs données sensibles
        
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * ❌ VULNÉRABILITÉ 4 : Fonction sensible sans protection
     * Promouvoir un utilisateur en admin sans vérification
     */
    @PostMapping("/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long id) {
        // ⚠️ DANGER : Pas de vérification de rôle
        // N'importe qui peut promouvoir n'importe qui en admin
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        // Simulation : ajout du rôle ADMIN
        // (en réalité, nécessiterait plus de logique)
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur promu administrateur");
        response.put("userId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * ❌ VULNÉRABILITÉ 5 : Énumération d'IDs
     * Les IDs séquentiels permettent d'énumérer tous les comptes
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<?> checkUserExists(@PathVariable Long id) {
        // ⚠️ DANGER : Permet de découvrir quels IDs existent
        // Un attaquant peut itérer de 1 à N pour trouver tous les comptes
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("exists", userRepository.existsById(id));
        
        return ResponseEntity.ok(response);
    }
}
