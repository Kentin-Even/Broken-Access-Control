package fr.ensitech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application de démonstration : Broken Access Control (A01:2021)
 * 
 * Cette application contient :
 * - Des endpoints VULNÉRABLES (/vulnerable/**) pour démonstration
 * - Des endpoints SÉCURISÉS (/secure/**) avec contre-mesures
 * 
 * Pour tester :
 * 1. Démarrer l'application : mvn spring-boot:run
 * 2. Voir le fichier TEST_SCENARIOS.md pour les tests
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Application démarrée avec succès !");
        System.out.println("=".repeat(80));
        System.out.println("\n📚 Endpoints disponibles :");
        System.out.println("   - http://localhost:8080/h2-console (Base de données H2)");
        System.out.println("   - http://localhost:8080/vulnerable/** (Endpoints vulnérables)");
        System.out.println("   - http://localhost:8080/secure/** (Endpoints sécurisés)");
        System.out.println("\n👤 Utilisateurs de test :");
        System.out.println("   - user@example.com / password123 (rôle USER)");
        System.out.println("   - admin@example.com / admin123 (rôle ADMIN)");
        System.out.println("\n📖 Consulter TEST_SCENARIOS.md pour les scénarios de test");
        System.out.println("=".repeat(80) + "\n");
    }
}
