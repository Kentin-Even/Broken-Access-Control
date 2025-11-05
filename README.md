# 🛡️ Broken Access Control Demo

## 📖 Description

Application de démonstration des vulnérabilités **Broken Access Control (A01:2021 OWASP Top 10)** avec exemples vulnérables et sécurisés en **Spring Boot**.

Projet créé dans le cadre du devoir M1-DI sur l'analyse pédagogique des risques OWASP.

---

## 🎯 Objectifs pédagogiques

1. **Comprendre** les attaques Broken Access Control
2. **Exploiter** des endpoints vulnérables (à des fins éducatives)
3. **Implémenter** des contre-mesures efficaces
4. **Comparer** code vulnérable vs code sécurisé

---

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/fr/ensitech/
│   │   ├── Application.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # Configuration Spring Security
│   │   │   └── DataInitializer.java         # Données de test
│   │   ├── controller/
│   │   │   ├── vulnerable/                  # ❌ Endpoints VULNÉRABLES
│   │   │   │   └── VulnerableUserController.java
│   │   │   ├── secure/                      # ✅ Endpoints SÉCURISÉS
│   │   │   │   └── SecureUserController.java
│   │   │   └── TestController.java
│   │   ├── dto/                             # DTOs pour protection Mass Assignment
│   │   │   ├── UserProfileUpdateDTO.java
│   │   │   └── UserResponseDTO.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── RoleRepository.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   └── security/
│   │       └── CustomUserDetailsService.java
│   └── resources/
│       └── application.yml
└── test/ (à venir)
```

---

## 🚀 Démarrage rapide

### Prérequis
- Java 21+
- Maven 3.8+

### Installation et lancement

```bash
cd broken-access-control-demo
mvn clean install
mvn spring-boot:run
```

L'application démarre sur **http://localhost:8080**

---

## 👥 Utilisateurs de test

| Email | Mot de passe | Rôles | Description |
|-------|-------------|-------|-------------|
| `user@example.com` | `password123` | USER | Utilisateur standard |
| `admin@example.com` | `admin123` | USER, ADMIN | Administrateur |
| `alice@example.com` | `alice123` | USER | Utilisateur standard |

---

## 🧪 Tests

### Vérifier que l'application fonctionne

```bash
curl http://localhost:8080/test
```

### Tests rapides

**1. Test endpoint vulnérable (Mass Assignment) :**
```bash
curl -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{"accountBalance": 999999, "roles": [{"name": "ROLE_ADMIN"}]}'
```
→ ❌ Réussit ! Le solde et les rôles sont modifiés

**2. Test endpoint sécurisé (Mass Assignment bloqué) :**
```bash
curl -X PUT http://localhost:8080/secure/users/1 \
  -u user@example.com:password123 \
  -H "Content-Type: application/json" \
  -d '{"firstName": "John", "lastName": "Doe", "accountBalance": 999999}'
```
→ ✅ Bloqué ! Seuls firstName et lastName sont mis à jour

**3. Test IDOR sur endpoint vulnérable :**
```bash
curl http://localhost:8080/vulnerable/users/3
```
→ ❌ Réussit ! On peut voir les données d'Alice

**4. Test IDOR sur endpoint sécurisé :**
```bash
curl http://localhost:8080/secure/users/3 \
  -u user@example.com:password123
```
→ ✅ Bloqué ! Erreur 403 Forbidden

---

## 📚 Documentation complète

- **[TEST_SCENARIOS.md](TEST_SCENARIOS.md)** : Guide complet de tous les scénarios de test
- **[Document technique](analyse_owasp_broken_access_control.md)** : Analyse détaillée OWASP
- **[Présentation PowerPoint](presentation_owasp_broken_access_control.pptx)** : Support de présentation

---

## 🎯 Vulnérabilités démontrées

### Endpoints `/vulnerable/**` (sans protection)

1. **Mass Assignment** : Modification de champs sensibles (roles, accountBalance)
2. **IDOR** : Accès aux données d'autres utilisateurs
3. **Missing Function Level Access Control** : Pas de vérification de rôles
4. **Information Disclosure** : Exposition de données sensibles
5. **Énumération d'IDs** : IDs séquentiels prévisibles

### Protections sur `/secure/**`

1. ✅ **DTOs avec whitelist** : Impossible de modifier des champs non autorisés
2. ✅ **Vérification de propriété** : Un utilisateur ne peut accéder qu'à ses données
3. ✅ **@PreAuthorize** : Contrôle d'accès basé sur les rôles (RBAC)
4. ✅ **Validation des entrées** : @Valid sur tous les DTOs
5. ✅ **Logs de sécurité** : Traçage des tentatives d'accès non autorisé

---

## 🔧 Technologies utilisées

- **Spring Boot 3.2.0**
- **Spring Security 6.x**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **JWT** (préparé, non encore implémenté)
- **Lombok**
- **Maven**

---

## 📊 Comparaison Vulnérable vs Sécurisé

| Fonctionnalité | `/vulnerable/` | `/secure/` |
|----------------|----------------|------------|
| Authentification | ❌ Aucune | ✅ Obligatoire |
| Vérification propriété | ❌ Non | ✅ Oui |
| Protection Mass Assignment | ❌ Non | ✅ DTOs |
| Contrôle rôles | ❌ Non | ✅ @PreAuthorize |
| Logs sécurité | ❌ Non | ✅ Oui |
| Validation entrées | ❌ Non | ✅ @Valid |

---

## 🛡️ Contre-mesures implémentées

### 1. Utilisation de DTOs

**Vulnérable :**
```java
@PutMapping("/{id}")
public User update(@RequestBody User user) {
    return repository.save(user); // DANGER !
}
```

**Sécurisé :**
```java
@PutMapping("/{id}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> update(@PathVariable Long id,
                                @Valid @RequestBody UserProfileUpdateDTO dto,
                                Authentication auth) {
    // Vérification de propriété
    if (!currentUser.getId().equals(id)) {
        return ResponseEntity.status(403).body("Accès refusé");
    }
    // Mise à jour sécurisée
    return ResponseEntity.ok(service.updateProfile(id, dto));
}
```

### 2. Vérification de propriété de ressource

```java
// ✅ Vérifier que l'utilisateur actuel possède la ressource
if (!currentUser.getId().equals(id)) {
    System.err.println("🚨 SECURITY ALERT: User " + currentUserEmail 
        + " tentative d'accès au profil " + id);
    return ResponseEntity.status(403).body("Accès refusé");
}
```

### 3. Contrôle d'accès basé sur les rôles

```java
@GetMapping("/all")
@PreAuthorize("hasRole('ADMIN')")  // ✅ Réservé aux admins
public ResponseEntity<?> getAllUsers() {
    // ...
}
```

---

## 📝 Utilisation avec Burp Suite

1. Configurer Burp en proxy (127.0.0.1:8080)
2. Intercepter les requêtes vers `/vulnerable/users/1`
3. Modifier le body JSON pour ajouter des champs non autorisés
4. Observer la différence avec `/secure/users/1`

Voir [TEST_SCENARIOS.md](TEST_SCENARIOS.md) pour plus de détails.

---

## 🔍 Console H2

Pour explorer la base de données :

1. Aller sur **http://localhost:8080/h2-console**
2. **JDBC URL** : `jdbc:h2:mem:testdb`
3. **Username** : `sa`
4. **Password** : *(laisser vide)*

Tables disponibles : `users`, `roles`, `user_roles`

---

## ⚠️ Avertissement

Cette application contient **volontairement** des vulnérabilités à des fins pédagogiques.

**❌ NE JAMAIS** déployer les endpoints `/vulnerable/**` en production.

Les techniques montrées ici sont destinées à :
- Comprendre les attaques
- Apprendre les contre-mesures
- Pratiquer les tests de sécurité

---

## 📖 Références

- **OWASP Top 10 2021** : https://owasp.org/Top10/
- **OWASP Cheat Sheets** : https://cheatsheetseries.owasp.org/
- **Spring Security Docs** : https://docs.spring.io/spring-security/reference/
- **Cas réel FIA F1** : https://ian.sh/fia

---

## 👨‍🎓 Auteur

Projet réalisé dans le cadre du M1-DI Full Stack Development - ENSITECH 2025

---

## 📄 Licence

Ce projet est à but éducatif uniquement.
