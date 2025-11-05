package fr.ensitech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO pour mise à jour de profil utilisateur
 * 
 * ✅ SÉCURISÉ : 
 * - Contient uniquement les champs modifiables par l'utilisateur
 * - Validation stricte sur chaque champ
 * - Aucun champ sensible (roles, accountBalance, passwordHash)
 * 
 * Protection contre Mass Assignment :
 * - Whitelist explicite des attributs autorisés
 * - Impossible d'injecter des champs non déclarés
 */
@Data
public class UserProfileUpdateDTO {
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;
    
    // ❌ ABSENTS VOLONTAIREMENT :
    // - roles : ne peut pas être modifié par l'utilisateur
    // - accountBalance : ne peut pas être modifié directement
    // - passwordHash : nécessite un endpoint dédié avec validation
    // - isActive : réservé aux administrateurs
    // - passportNumber, socialSecurityNumber : données sensibles non modifiables
}
