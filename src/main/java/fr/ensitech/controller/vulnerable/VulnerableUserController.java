package fr.ensitech.controller.vulnerable;

import fr.ensitech.model.Role;
import fr.ensitech.model.User;
import fr.ensitech.repository.RoleRepository;
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
    private final RoleRepository roleRepository;

    public VulnerableUserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * ❌ VULNÉRABILITÉ 1 : Mass Assignment
     * L'entité User complète est exposée, permettant de modifier TOUS les champs
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        // ⚠️ DANGER : Aucune vérification
        // Un attaquant peut envoyer n'importe quel champ : roles, accountBalance, etc.

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Mise à jour de tous les champs (VULNÉRABLE)
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setPassportNumber(user.getPassportNumber());
        existingUser.setSocialSecurityNumber(user.getSocialSecurityNumber());

        // ⚠️ VULNÉRABILITÉ : Permet de modifier le solde du compte
        if (user.getAccountBalance() != null) {
            existingUser.setAccountBalance(user.getAccountBalance());
            System.out.println("⚠️ SECURITY BREACH: Account balance modified to " + user.getAccountBalance());
        }

        // ⚠️ VULNÉRABILITÉ : Permet de modifier le statut actif
        existingUser.setActive(user.isActive());

        // ⚠️ VULNÉRABILITÉ : Permet de modifier les rôles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.getRoles().clear();
            existingUser.getRoles().addAll(user.getRoles());
            System.out.println("⚠️ SECURITY BREACH: Roles modified to " + user.getRoles());
        }

        User savedUser = userRepository.save(existingUser);

        return ResponseEntity.ok(savedUser);
    }

    /**
     * ❌ VULNÉRABILITÉ : Endpoint pour promouvoir directement aux rôles admin
     * Simplifie l'attaque Mass Assignment pour les rôles
     */
    @PostMapping("/{id}/add-role/{roleName}")
    public ResponseEntity<?> addRoleToUser(@PathVariable Long id, @PathVariable String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        user.getRoles().add(role);
        userRepository.save(user);

        System.out.println("⚠️ SECURITY BREACH: Role " + roleName + " added to user " + id);

        return ResponseEntity.ok(Map.of(
                "message", "Rôle ajouté avec succès",
                "userId", id,
                "role", roleName
        ));
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

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rôle ADMIN introuvable"));

        user.getRoles().add(adminRole);
        userRepository.save(user);

        System.out.println("⚠️ SECURITY BREACH: User " + id + " promoted to ADMIN");

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