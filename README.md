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
- **Java 21** (IMPORTANT : Utiliser Java 21, pas Java 25)
- Maven 3.8+ OU utiliser le Maven Wrapper inclus

### Installation et lancement

**Option 1 : Avec Maven installé**
```bash
cd broken-access-control-demo
mvn clean install
mvn spring-boot:run
```

**Option 2 : Avec Maven Wrapper (si Maven non installé)**
```bash
cd broken-access-control-demo
chmod +x mvnw
./mvnw clean install
./mvnw spring-boot:run
```

**Option 3 : Avec IntelliJ IDEA**
1. Ouvrir le projet dans IntelliJ
2. Vérifier que le SDK est bien Java 21
3. Clic droit sur `Application.java` → **Run 'Application'**

L'application démarre sur **http://localhost:8080**

---

## 👥 Utilisateurs de test

| Email | Mot de passe | Rôles | ID | Solde initial |
|-------|-------------|-------|-----|---------------|
| `user@example.com` | `password123` | ROLE_USER | 1 | 1000€ |
| `admin@example.com` | `admin123` | ROLE_USER, ROLE_ADMIN | 2 | 5000€ |
| `alice@example.com` | `alice123` | ROLE_USER | 3 | 2500€ |

---

## 🧪 Tests rapides

### ✅ Vérifier que l'application fonctionne

```bash
curl http://localhost:8080/test
```

**Réponse attendue :**
```json
{
  "status": "OK",
  "message": "Application fonctionne correctement"
}
```

---

## ⚠️ Tests des endpoints VULNÉRABLES

### Test 1 : Mass Assignment - Modifier le solde du compte

**❌ Exploitation :**
```bash
# Voir l'état initial
curl http://localhost:8080/vulnerable/users/1 | jq '.accountBalance, .roles'

# Attaque : Modifier le solde à 999999€
curl -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+33612345678",
    "accountBalance": 999999.99,
    "active": true
  }'

# Vérifier que l'attaque a réussi
curl http://localhost:8080/vulnerable/users/1 | jq '.accountBalance'
```

**Résultat :** ✅ **L'attaque réussit !** Le solde passe à 999999.99€

---

### Test 2 : Mass Assignment - S'ajouter le rôle ADMIN

**❌ Exploitation :**
```bash
# Attaque : Ajouter le rôle ADMIN
curl -X POST http://localhost:8080/vulnerable/users/1/promote

# Vérifier les rôles
curl http://localhost:8080/vulnerable/users/1 | jq '.roles'
```

**Résultat :** ✅ **L'attaque réussit !** L'utilisateur a maintenant ROLE_ADMIN

**Note :** Dans la console, tu verras :
```
⚠️ SECURITY BREACH: User 1 promoted to ADMIN
```

---

### Test 3 : IDOR - Accéder aux données d'Alice

**❌ Exploitation :**
```bash
# Attaque : Accéder aux données sensibles d'Alice (user 3)
curl http://localhost:8080/vulnerable/users/3 | jq

# Voir son passport et SSN
curl http://localhost:8080/vulnerable/users/3 | jq '.passportNumber, .socialSecurityNumber, .accountBalance'
```

**Résultat :** ✅ **L'attaque réussit !** On peut voir toutes les données sensibles d'Alice

---

### Test 4 : Missing Function Level Access Control

**❌ Exploitation :**
```bash
# Attaque : Lister tous les utilisateurs sans authentification
curl http://localhost:8080/vulnerable/users/all | jq
```

**Résultat :** ✅ **L'attaque réussit !** On peut voir tous les utilisateurs avec leurs données sensibles

---

### Test 5 : Énumération d'IDs

**❌ Exploitation :**
```bash
# Attaque : Découvrir quels IDs existent
for i in {1..10}; do
  echo "Testing ID $i:"
  curl http://localhost:8080/vulnerable/users/exists/$i
  echo ""
done
```

**Résultat :** ✅ **L'attaque réussit !** On découvre que les IDs 1, 2, 3 existent

---

## ✅ Tests des endpoints SÉCURISÉS

### Test 1 : Mass Assignment bloqué

**✅ Protection :**
```bash
# Tentative : Modifier le solde et ajouter ROLE_ADMIN
curl -X PUT http://localhost:8080/secure/users/1 \
  -u user@example.com:password123 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "phoneNumber": "+33612345678",
    "accountBalance": 999999.99,
    "roles": [{"name": "ROLE_ADMIN"}]
  }'

# Vérifier que le solde et les rôles n'ont PAS changé
curl http://localhost:8080/secure/users/1 \
  -u user@example.com:password123 | jq
```

**Résultat :** ✅ **Protection efficace !** Les champs `accountBalance` et `roles` sont ignorés (pas dans le DTO)

---

### Test 2 : IDOR bloqué

**✅ Protection :**
```bash
# Tentative : User 1 essaie d'accéder au profil de User 3
curl http://localhost:8080/secure/users/3 \
  -u user@example.com:password123
```

**Résultat :** ✅ **Protection efficace !** Erreur 403 Forbidden
```json
{
  "error": "Accès refusé",
  "message": "Vous ne pouvez accéder qu'à votre propre profil"
}
```

Dans la console :
```
🚨 SECURITY ALERT: User user@example.com tentative d'accès au profil 3
```

---

### Test 3 : Accès à son propre profil (autorisé)

**✅ Fonctionnement normal :**
```bash
# User 1 accède à son propre profil
curl http://localhost:8080/secure/users/1 \
  -u user@example.com:password123 | jq
```

**Résultat :** ✅ **Succès !** L'utilisateur peut voir son propre profil

---

### Test 4 : Endpoint admin protégé

**✅ Protection :**
```bash
# User normal essaie de lister tous les utilisateurs
curl http://localhost:8080/secure/users/all \
  -u user@example.com:password123
```

**Résultat :** ✅ **Protection efficace !** Erreur 403 Forbidden

**Avec un compte admin :**
```bash
# Admin liste tous les utilisateurs
curl http://localhost:8080/secure/users/all \
  -u admin@example.com:admin123 | jq
```

**Résultat :** ✅ **Succès !** L'admin peut voir la liste

---

### Test 5 : Promotion protégée

**✅ Protection :**
```bash
# User normal essaie de promouvoir quelqu'un
curl -X POST http://localhost:8080/secure/users/3/promote \
  -u user@example.com:password123
```

**Résultat :** ✅ **Protection efficace !** Erreur 403 Forbidden

**Avec un compte admin :**
```bash
# Admin promeut un utilisateur
curl -X POST http://localhost:8080/secure/users/1/promote \
  -u admin@example.com:admin123
```

**Résultat :** ✅ **Succès !** Log d'audit :
```
📋 AUDIT: Admin admin@example.com promoting user 1 to ADMIN
```

---

### Test 6 : Accès sans authentification

**✅ Protection :**
```bash
# Tentative d'accès sans credentials
curl http://localhost:8080/secure/users/1
```

**Résultat :** ✅ **Protection efficace !** Erreur 401 Unauthorized

---

## 🎯 Démonstration complète pour la présentation

```bash
#!/bin/bash

echo "=== DÉMONSTRATION BROKEN ACCESS CONTROL ==="
echo ""

echo "1️⃣ État initial de l'utilisateur 1:"
curl -s http://localhost:8080/vulnerable/users/1 | jq '{id, email, accountBalance, roles}'
echo ""

echo "2️⃣ ATTAQUE : Modification du solde à 999999€"
curl -s -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{"id":1,"email":"user@example.com","firstName":"John","lastName":"Doe","phoneNumber":"+33612345678","accountBalance":999999.99,"active":true}' \
  | jq '{id, email, accountBalance}'
echo ""

echo "3️⃣ ATTAQUE : Promotion en ADMIN"
curl -s -X POST http://localhost:8080/vulnerable/users/1/promote | jq
echo ""

echo "4️⃣ Vérification - L'utilisateur est maintenant riche et admin !"
curl -s http://localhost:8080/vulnerable/users/1 | jq '{id, email, accountBalance, roles}'
echo ""

echo "5️⃣ ATTAQUE IDOR : Accès aux données d'Alice (user 3)"
curl -s http://localhost:8080/vulnerable/users/3 | jq '{id, email, passportNumber, socialSecurityNumber, accountBalance}'
echo ""

echo "6️⃣ ATTAQUE : Lister tous les utilisateurs sans auth"
curl -s http://localhost:8080/vulnerable/users/all | jq '[.[] | {id, email, accountBalance}]'
echo ""

echo "=== COMPARAISON AVEC PROTECTIONS ==="
echo ""

echo "7️⃣ PROTECTION : Tentative Mass Assignment (bloquée)"
curl -s -X PUT http://localhost:8080/secure/users/1 \
  -u user@example.com:password123 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","accountBalance":999999.99}' \
  | jq
echo ""

echo "8️⃣ PROTECTION : Tentative IDOR (bloquée)"
curl -s http://localhost:8080/secure/users/3 \
  -u user@example.com:password123 \
  | jq
```

---

## 📊 Comparaison Vulnérable vs Sécurisé

| Fonctionnalité | `/vulnerable/` | `/secure/` |
|----------------|----------------|------------|
| Authentification | ❌ Aucune | ✅ Obligatoire (Basic Auth) |
| Vérification propriété | ❌ Non | ✅ Oui (user = ressource) |
| Protection Mass Assignment | ❌ Non | ✅ DTOs avec whitelist |
| Contrôle rôles | ❌ Non | ✅ @PreAuthorize |
| Logs sécurité | ❌ Non | ✅ Oui (tentatives d'accès) |
| Validation entrées | ❌ Non | ✅ @Valid sur DTOs |
| Exposition données sensibles | ❌ Toutes | ✅ DTOs filtrent |

---

## 🛡️ Contre-mesures implémentées

### 1. Utilisation de DTOs

**❌ Vulnérable :**
```java
@PutMapping("/{id}")
public User update(@RequestBody User user) {
    return repository.save(user); // DANGER !
}
```

**✅ Sécurisé :**
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

### Configuration
1. Configurer Burp en proxy (127.0.0.1:8080)
2. Configurer le navigateur pour utiliser ce proxy
3. Intercepter les requêtes vers `/vulnerable/users/1`
4. Modifier le body JSON pour ajouter des champs non autorisés
5. Observer la différence avec `/secure/users/1`

### Test Mass Assignment avec Burp
1. Intercepter `PUT /vulnerable/users/1`
2. Modifier le body pour ajouter `"accountBalance": 999999`
3. Observer que ça fonctionne sur `/vulnerable/` mais pas sur `/secure/`

---

## 🔍 Console H2

Pour explorer la base de données :

1. Aller sur **http://localhost:8080/h2-console**
2. **JDBC URL** : `jdbc:h2:mem:testdb`
3. **Username** : `sa`
4. **Password** : *(laisser vide)*

### Requêtes SQL utiles :

```sql
-- Voir tous les utilisateurs
SELECT * FROM users;

-- Voir tous les rôles
SELECT * FROM roles;

-- Voir la table de liaison
SELECT * FROM user_roles;

-- Voir les utilisateurs avec leurs rôles
SELECT u.id, u.email, u.first_name, u.account_balance, r.name as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id;
```

---

## 📚 Documentation complète

- **[TEST_SCENARIOS.md](TEST_SCENARIOS.md)** : Guide complet de tous les scénarios de test
- **[TEST_COMMANDS.md](TEST_COMMANDS.md)** : Commandes copier/coller pour tests rapides
- **[analyse_owasp_broken_access_control.md](analyse_owasp_broken_access_control.md)** : Analyse détaillée OWASP
- **[presentation_owasp_broken_access_control.pptx](presentation_owasp_broken_access_control.pptx)** : Support de présentation

---

## 🎯 Vulnérabilités démontrées

### Endpoints `/vulnerable/**` (sans protection)

1. ✅ **Mass Assignment** : Modification de champs sensibles (roles, accountBalance)
2. ✅ **IDOR** : Accès aux données d'autres utilisateurs
3. ✅ **Missing Function Level Access Control** : Pas de vérification de rôles
4. ✅ **Information Disclosure** : Exposition de données sensibles
5. ✅ **Énumération d'IDs** : IDs séquentiels prévisibles

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
- **Lombok**
- **Maven**
- **Java 21**

---

## ⚠️ Avertissement

Cette application contient **volontairement** des vulnérabilités à des fins pédagogiques.

**❌ NE JAMAIS** déployer les endpoints `/vulnerable/**` en production.

Les techniques montrées ici sont destinées à :
- Comprendre les attaques
- Apprendre les contre-mesures
- Pratiquer les tests de sécurité

---

## 🐛 Dépannage

### Problème : "Cannot find symbol: method setIsActive"
**Solution :** Utiliser Java 21 au lieu de Java 25. Voir [FIX_JAVA_LOMBOK_ERROR.md](FIX_JAVA_LOMBOK_ERROR.md)

### Problème : H2 database not found
**Solution :** Utiliser `jdbc:h2:mem:testdb` (PAS `jdbc:h2:~/test`)

### Problème : Maven not found
**Solution :** Utiliser `./mvnw` au lieu de `mvn`, ou installer Maven avec `brew install maven`

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