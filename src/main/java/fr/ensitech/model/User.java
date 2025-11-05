package fr.ensitech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    private String phoneNumber;
    
    // ⚠️ Champ sensible - ne JAMAIS exposer dans les API
    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;
    
    // ⚠️ Champ sensible - simulation d'un solde de compte
    private Double accountBalance = 0.0;
    
    // ⚠️ Champs sensibles - rôles et permissions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    // Simulation de données sensibles supplémentaires
    private String passportNumber;
    private String socialSecurityNumber;
}
