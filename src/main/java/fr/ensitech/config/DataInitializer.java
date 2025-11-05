package fr.ensitech.config;

import fr.ensitech.model.Role;
import fr.ensitech.model.User;
import fr.ensitech.repository.RoleRepository;
import fr.ensitech.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initialisation des données de test
 * 
 * Crée :
 * - 2 rôles : ROLE_USER, ROLE_ADMIN
 * - 3 utilisateurs de test
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                     RoleRepository roleRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            
            // Création des rôles
            if (roleRepository.count() == 0) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                userRole.setDescription("Utilisateur standard");
                roleRepository.save(userRole);
                
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole.setDescription("Administrateur");
                roleRepository.save(adminRole);
                
                System.out.println("✅ Rôles créés");
            }
            
            // Création des utilisateurs de test
            if (userRepository.count() == 0) {
                Role userRole = roleRepository.findByName("ROLE_USER").get();
                Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
                
                // Utilisateur 1 : User normal
                User user1 = new User();
                user1.setEmail("user@example.com");
                user1.setPasswordHash(passwordEncoder.encode("password123"));
                user1.setFirstName("John");
                user1.setLastName("Doe");
                user1.setPhoneNumber("+33612345678");
                user1.setAccountBalance(1000.0);
                user1.setPassportNumber("FR123456789");
                user1.getRoles().add(userRole);
                userRepository.save(user1);
                
                // Utilisateur 2 : Admin
                User user2 = new User();
                user2.setEmail("admin@example.com");
                user2.setPasswordHash(passwordEncoder.encode("admin123"));
                user2.setFirstName("Jane");
                user2.setLastName("Smith");
                user2.setPhoneNumber("+33698765432");
                user2.setAccountBalance(5000.0);
                user2.setPassportNumber("FR987654321");
                user2.getRoles().add(userRole);
                user2.getRoles().add(adminRole);
                userRepository.save(user2);
                
                // Utilisateur 3 : User normal (pour tester IDOR)
                User user3 = new User();
                user3.setEmail("alice@example.com");
                user3.setPasswordHash(passwordEncoder.encode("alice123"));
                user3.setFirstName("Alice");
                user3.setLastName("Johnson");
                user3.setPhoneNumber("+33656781234");
                user3.setAccountBalance(2500.0);
                user3.setPassportNumber("FR456789123");
                user3.setSocialSecurityNumber("1234567890123");
                user3.getRoles().add(userRole);
                userRepository.save(user3);
                
                System.out.println("\n" + "=".repeat(80));
                System.out.println("✅ Utilisateurs de test créés :");
                System.out.println("   1. user@example.com / password123 (ROLE_USER) - ID: " + user1.getId());
                System.out.println("   2. admin@example.com / admin123 (ROLE_ADMIN) - ID: " + user2.getId());
                System.out.println("   3. alice@example.com / alice123 (ROLE_USER) - ID: " + user3.getId());
                System.out.println("=".repeat(80) + "\n");
            }
        };
    }
}
