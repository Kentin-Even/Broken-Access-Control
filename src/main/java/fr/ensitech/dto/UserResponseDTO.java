package fr.ensitech.dto;

import fr.ensitech.model.User;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO pour les réponses API
 * 
 * ✅ SÉCURISÉ :
 * - Ne contient QUE les informations non sensibles
 * - Pas de passwordHash, accountBalance détaillé, données personnelles sensibles
 * - Les rôles ne sont exposés que si nécessaire
 */
@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean active;
    
    // Optionnel : rôles (uniquement pour l'utilisateur lui-même ou admin)
    private Set<String> roles;
    
    // ❌ ABSENTS pour sécurité :
    // - passwordHash
    // - accountBalance (sauf si demandé explicitement dans un endpoint dédié)
    // - passportNumber
    // - socialSecurityNumber
    
    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setActive(user.isActive());
        return dto;
    }
    
    public static UserResponseDTO fromEntityWithRoles(User user) {
        UserResponseDTO dto = fromEntity(user);
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        return dto;
    }
}
