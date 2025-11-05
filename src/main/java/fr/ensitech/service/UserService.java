package fr.ensitech.service;

import fr.ensitech.dto.UserProfileUpdateDTO;
import fr.ensitech.model.Role;
import fr.ensitech.model.User;
import fr.ensitech.repository.RoleRepository;
import fr.ensitech.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, 
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Mise à jour du profil avec whitelist explicite
     * ✅ SÉCURISÉ : Seuls les champs du DTO sont modifiables
     */
    @Transactional
    public User updateProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        // ✅ Mise à jour UNIQUEMENT des champs autorisés
        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setEmail(updateDTO.getEmail());
        user.setPhoneNumber(updateDTO.getPhoneNumber());
        
        // ❌ Impossible de modifier :
        // - roles (pas dans le DTO)
        // - accountBalance (pas dans le DTO)
        // - passwordHash (pas dans le DTO)
        // - isActive (pas dans le DTO)
        
        return userRepository.save(user);
    }
    
    /**
     * Promotion d'un utilisateur (admin uniquement)
     * Cette méthode doit être protégée par @PreAuthorize("hasRole('ADMIN')")
     */
    @Transactional
    public User addAdminRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("Rôle ADMIN introuvable"));
        
        user.getRoles().add(adminRole);
        return userRepository.save(user);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * Création d'un utilisateur (pour initialisation)
     */
    @Transactional
    public User createUser(String email, String password, String firstName, String lastName, String roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAccountBalance(1000.0); // Solde initial
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
        user.getRoles().add(role);
        
        return userRepository.save(user);
    }
}
