package fr.ensitech.controller.secure;

import fr.ensitech.dto.UserProfileUpdateDTO;
import fr.ensitech.dto.UserResponseDTO;
import fr.ensitech.model.User;
import fr.ensitech.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ CONTROLLER SÉCURISÉ
 * 
 * Mesures de sécurité implémentées :
 * 1. Utilisation de DTOs au lieu d'entités (protection Mass Assignment)
 * 2. Vérification de propriété de ressource (protection IDOR)
 * 3. Validation des entrées (@Valid)
 * 4. Autorisation basée sur les rôles (@PreAuthorize)
 * 5. Pas d'exposition de données sensibles dans les réponses
 */
@RestController
@RequestMapping("/secure/users")
@CrossOrigin(origins = "*")
public class SecureUserController {

    private final UserService userService;
    
    public SecureUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ✅ SÉCURISÉ : Mise à jour du profil utilisateur
     * 
     * Protections :
     * - Authentification obligatoire (@PreAuthorize)
     * - Vérification de propriété (l'utilisateur ne peut modifier que son propre profil)
     * - Utilisation de DTO avec whitelist de champs
     * - Validation des entrées (@Valid)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO,
            Authentication authentication) {
        
        // Récupération de l'utilisateur authentifié
        String currentUserEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentUserEmail);
        
        // ✅ VÉRIFICATION CRITIQUE : L'utilisateur ne peut modifier que son propre profil
        if (!currentUser.getId().equals(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Accès refusé");
            error.put("message", "Vous ne pouvez modifier que votre propre profil");
            
            // Log de la tentative d'accès non autorisé
            System.err.println("🚨 SECURITY ALERT: User " + currentUserEmail 
                + " tentative d'accès au profil " + id);
            
            return ResponseEntity.status(403).body(error);
        }
        
        // Mise à jour sécurisée (uniquement les champs du DTO)
        User updatedUser = userService.updateProfile(id, updateDTO);
        
        // Retour d'un DTO pour ne pas exposer les champs sensibles
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }
    
    /**
     * ✅ SÉCURISÉ : Récupération du profil
     * 
     * Protections :
     * - Vérification de propriété
     * - DTO dans la réponse (pas de données sensibles)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id, Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentUserEmail);
        
        // ✅ Vérification de propriété
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Accès refusé",
                "message", "Vous ne pouvez accéder qu'à votre propre profil"
            ));
        }
        
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponseDTO.fromEntityWithRoles(user));
    }
    
    /**
     * ✅ SÉCURISÉ : Liste de tous les utilisateurs (ADMIN uniquement)
     * 
     * Protections :
     * - Réservé aux administrateurs (@PreAuthorize("hasRole('ADMIN')"))
     * - DTO dans les réponses
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        
        List<User> users = userService.findAll();
        List<UserResponseDTO> usersDTO = users.stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usersDTO);
    }
    
    /**
     * ✅ SÉCURISÉ : Promotion d'un utilisateur (ADMIN uniquement)
     * 
     * Protections :
     * - Réservé aux administrateurs
     * - Log de l'action pour audit
     */
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteUser(@PathVariable Long id, Authentication authentication) {
        
        // Log de l'action sensible pour audit
        System.out.println("📋 AUDIT: Admin " + authentication.getName() 
            + " promoting user " + id + " to ADMIN");
        
        userService.addAdminRole(id);
        
        return ResponseEntity.ok(Map.of(
            "message", "Utilisateur promu administrateur",
            "userId", id
        ));
    }
    
    /**
     * ✅ SÉCURISÉ : Récupération du profil de l'utilisateur connecté
     * 
     * Utilise l'identité de l'utilisateur authentifié
     * Pas d'ID dans l'URL = pas d'IDOR possible
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentUserEmail);
        
        return ResponseEntity.ok(UserResponseDTO.fromEntityWithRoles(currentUser));
    }
}
