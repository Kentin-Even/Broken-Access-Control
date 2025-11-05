# 🧪 Guide de test - Broken Access Control Demo

## 📋 Table des matières
1. [Démarrage de l'application](#démarrage)
2. [Utilisateurs de test](#utilisateurs)
3. [Tests des endpoints VULNÉRABLES](#tests-vulnérables)
4. [Tests des endpoints SÉCURISÉS](#tests-sécurisés)
5. [Utilisation avec Burp Suite](#burp-suite)

---

## 🚀 Démarrage de l'application {#démarrage}

### Prérequis
- Java 21+
- Maven 3.8+

### Lancer l'application

```bash
cd broken-access-control-demo
mvn clean install
mvn spring-boot:run
```

L'application démarre sur **http://localhost:8080**

### Vérifier que ça fonctionne

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

## 👥 Utilisateurs de test {#utilisateurs}

| Email | Mot de passe | Rôles | ID |
|-------|-------------|-------|-----|
| user@example.com | password123 | ROLE_USER | 1 |
| admin@example.com | admin123 | ROLE_USER, ROLE_ADMIN | 2 |
| alice@example.com | alice123 | ROLE_USER | 3 |

---

## ⚠️ Tests des endpoints VULNÉRABLES {#tests-vulnérables}

### Test 1 : Mass Assignment Attack

**Objectif :** Modifier des champs sensibles (rôles, solde) via une requête PUT

**Requête :**
```bash
curl -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "accountBalance": 999999.99,
    "roles": [
      {
        "id": 2,
        "name": "ROLE_ADMIN",
        "description": "Administrateur"
      }
    ]
  }'
```

**Résultat attendu :**
✅ L'attaque réussit ! Le solde et les rôles sont modifiés.

**Vérification :**
```bash
curl http://localhost:8080/vulnerable/users/1
```

Tu verras que `accountBalance` = 999999.99 et l'utilisateur a maintenant `ROLE_ADMIN`.

---

### Test 2 : IDOR (Insecure Direct Object Reference)

**Objectif :** Accéder aux données d'autres utilisateurs en changeant l'ID

**Scénario :** User 1 essaie d'accéder aux données de User 3

```bash
# Accéder à son propre profil (légitime)
curl http://localhost:8080/vulnerable/users/1

# Accéder au profil d'un autre utilisateur (IDOR)
curl http://localhost:8080/vulnerable/users/3
```

**Résultat attendu :**
✅ L'attaque réussit ! On peut voir les données d'Alice (user 3) : passport, SSN, solde, etc.

---

### Test 3 : Missing Function Level Access Control

**Objectif :** Accéder à un endpoint admin sans être admin

```bash
# Lister tous les utilisateurs (devrait être réservé aux admins)
curl http://localhost:8080/vulnerable/users/all
```

**Résultat attendu :**
✅ L'attaque réussit ! N'importe qui peut lister tous les utilisateurs avec leurs données sensibles.

---

### Test 4 : Promotion non autorisée

**Objectif :** Se promouvoir soi-même en admin

```bash
curl -X POST http://localhost:8080/vulnerable/users/1/promote
```

**Résultat attendu :**
✅ L'attaque réussit ! L'utilisateur peut se promouvoir en admin sans vérification.

---

### Test 5 : Énumération d'IDs

**Objectif :** Découvrir tous les comptes existants

```bash
for i in {1..10}; do
  echo "Testing ID $i:"
  curl http://localhost:8080/vulnerable/users/exists/$i
  echo ""
done
```

**Résultat attendu :**
✅ L'attaque réussit ! On peut découvrir quels IDs existent (1, 2, 3 existent, 4+ n'existent pas).

---

## ✅ Tests des endpoints SÉCURISÉS {#tests-sécurisés}

### Test 1 : Mass Assignment bloqué

**Objectif :** Tenter de modifier des champs sensibles (devrait échouer)

**Requête avec authentification :**
```bash
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
```

**Résultat attendu :**
✅ **Protection efficace !**
- Les champs `accountBalance` et `roles` sont **ignorés** (pas dans le DTO)
- Seuls `firstName`, `lastName`, `email`, `phoneNumber` sont mis à jour
- Message d'erreur 400 si les champs ne passent pas la validation

**Vérification :**
```bash
curl http://localhost:8080/secure/users/1 \
  -u user@example.com:password123
```

Le solde et les rôles n'ont **pas changé**.

---

### Test 2 : Protection IDOR

**Objectif :** Essayer d'accéder au profil d'un autre utilisateur

```bash
# User 1 essaie d'accéder au profil de User 3
curl http://localhost:8080/secure/users/3 \
  -u user@example.com:password123
```

**Résultat attendu :**
✅ **Protection efficace !** Erreur 403 Forbidden
```json
{
  "error": "Accès refusé",
  "message": "Vous ne pouvez accéder qu'à votre propre profil"
}
```

Un log apparaît dans la console :
```
🚨 SECURITY ALERT: User user@example.com tentative d'accès au profil 3
```

---

### Test 3 : Accès admin protégé

**Objectif :** User normal essaie d'accéder à un endpoint admin

```bash
# User 1 (non-admin) essaie de lister tous les utilisateurs
curl http://localhost:8080/secure/users/all \
  -u user@example.com:password123
```

**Résultat attendu :**
✅ **Protection efficace !** Erreur 403 Forbidden

**Avec un compte admin :**
```bash
# Admin essaie de lister tous les utilisateurs
curl http://localhost:8080/secure/users/all \
  -u admin@example.com:admin123
```

**Résultat :** ✅ Succès ! L'admin peut voir la liste.

---

### Test 4 : Promotion protégée

**Objectif :** User normal essaie de promouvoir quelqu'un

```bash
# User 1 essaie de promouvoir User 3
curl -X POST http://localhost:8080/secure/users/3/promote \
  -u user@example.com:password123
```

**Résultat attendu :**
✅ **Protection efficace !** Erreur 403 Forbidden

**Avec un compte admin :**
```bash
curl -X POST http://localhost:8080/secure/users/1/promote \
  -u admin@example.com:admin123
```

**Résultat :** ✅ Succès ! Un log d'audit apparaît :
```
📋 AUDIT: Admin admin@example.com promoting user 1 to ADMIN
```

---

### Test 5 : Accès sans authentification

**Objectif :** Essayer d'accéder aux endpoints sécurisés sans credentials

```bash
curl http://localhost:8080/secure/users/1
```

**Résultat attendu :**
✅ **Protection efficace !** Erreur 401 Unauthorized

---

## 🛡️ Utilisation avec Burp Suite {#burp-suite}

### Configuration

1. **Démarrer Burp Suite**
2. **Configurer le proxy** : 127.0.0.1:8080
3. **Activer l'interception**

### Scénario de test avec Burp

#### Test Mass Assignment

1. Faire une requête légitime :
```
PUT /vulnerable/users/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe"
}
```

2. **Intercepter avec Burp** et modifier le body :
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "accountBalance": 999999,
  "roles": [{"name": "ROLE_ADMIN"}]
}
```

3. Observer que les champs `accountBalance` et `roles` sont acceptés sur `/vulnerable/` mais ignorés sur `/secure/`.

---

#### Test IDOR avec Burp Intruder

1. Intercepter une requête :
```
GET /vulnerable/users/1 HTTP/1.1
Host: localhost:8080
```

2. **Envoyer à Intruder** (Ctrl+I)
3. **Marquer l'ID comme payload position** : `/vulnerable/users/§1§`
4. **Payload type** : Numbers (1-100)
5. **Start attack**

Burp va énumérer tous les IDs et identifier lesquels existent.

---

#### Test de contournement d'authentification

1. Essayer d'accéder à `/secure/users/1` sans auth
2. Observer la réponse 401
3. Ajouter un header `Authorization: Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZDEyMw==`
   (user@example.com:password123 en base64)
4. Observer que l'accès fonctionne maintenant

---

## 📊 Tableau de comparaison

| Attaque | Endpoint Vulnérable | Endpoint Sécurisé |
|---------|---------------------|-------------------|
| Mass Assignment | ❌ Réussit | ✅ Bloqué (DTO) |
| IDOR | ❌ Réussit | ✅ Bloqué (vérif propriété) |
| Missing Auth | ❌ Réussit | ✅ Bloqué (@PreAuthorize) |
| Énumération IDs | ❌ Réussit | ✅ Empêché (UUIDs recommandés) |
| Élévation privilèges | ❌ Réussit | ✅ Bloqué (RBAC strict) |

---

## 🎯 Exercices pratiques

### Exercice 1 : Exploitation complète
1. Utiliser Mass Assignment pour te donner 1 million d'euros
2. Utiliser IDOR pour voler le passport d'Alice
3. Te promouvoir en admin
4. Lister tous les utilisateurs

### Exercice 2 : Vérification des protections
1. Tenter les mêmes attaques sur `/secure/**`
2. Noter les messages d'erreur
3. Vérifier les logs de sécurité dans la console

### Exercice 3 : Avec Burp Suite
1. Configurer Burp
2. Intercepter une requête PUT sur `/vulnerable/users/1`
3. Modifier le JSON pour ajouter `"accountBalance": 999999`
4. Comparer avec la même requête sur `/secure/users/1`

---

## 📝 Questions de réflexion

1. **Pourquoi les endpoints `/vulnerable/` acceptent-ils n'importe quel champ ?**
   → Parce que l'entité `User` complète est exposée directement

2. **Comment le DTO protège-t-il contre Mass Assignment ?**
   → Il définit une **whitelist** explicite de champs modifiables

3. **Pourquoi la vérification `currentUser.getId().equals(id)` est-elle critique ?**
   → Elle empêche un utilisateur d'accéder aux ressources d'un autre (IDOR)

4. **Que se passerait-il sans `@PreAuthorize("hasRole('ADMIN')")` ?**
   → N'importe quel utilisateur authentifié pourrait promouvoir des gens

5. **Pourquoi utiliser des UUIDs au lieu d'IDs séquentiels ?**
   → Pour empêcher l'énumération : impossible de deviner les IDs

---

## 🔍 Logs à surveiller

Lors des tests sur `/secure/**`, tu verras ces logs :

```
🚨 SECURITY ALERT: User user@example.com tentative d'accès au profil 3
📋 AUDIT: Admin admin@example.com promoting user 1 to ADMIN
```

Ces logs sont **essentiels** pour :
- Détecter les tentatives d'attaque
- Tracer les actions sensibles
- Répondre aux incidents de sécurité

---

## ✅ Checklist de validation

- [ ] Application démarre sans erreur
- [ ] Endpoints `/vulnerable/` accessibles sans auth
- [ ] Endpoints `/secure/` nécessitent auth
- [ ] Mass Assignment bloqué sur `/secure/`
- [ ] IDOR bloqué sur `/secure/`
- [ ] Promotion admin réservée aux admins
- [ ] Logs de sécurité visibles dans la console
- [ ] Console H2 accessible sur /h2-console

---

## 🎓 Pour aller plus loin

1. **Ajouter JWT** au lieu de Basic Auth
2. **Implémenter rate limiting** pour limiter les tentatives
3. **Ajouter des tests JUnit** pour valider les protections
4. **Utiliser des UUIDs** au lieu d'IDs séquentiels
5. **Ajouter un WAF** (Web Application Firewall)
6. **Implémenter 2FA** pour les actions sensibles

---

**Bon courage pour tes tests ! 🚀**
