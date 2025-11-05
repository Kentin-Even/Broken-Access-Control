#!/bin/bash

echo "========================================"
echo "  Broken Access Control Demo - Start   "
echo "========================================"
echo ""

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé. Veuillez installer Java 21+."
    exit 1
fi

echo "✅ Java version:"
java -version
echo ""

# Vérifier Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé. Veuillez installer Maven 3.8+."
    exit 1
fi

echo "✅ Maven version:"
mvn -version | head -n 1
echo ""

# Compiler et démarrer
echo "📦 Compilation du projet..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilation réussie !"
    echo ""
    echo "🚀 Démarrage de l'application..."
    echo ""
    mvn spring-boot:run
else
    echo ""
    echo "❌ Erreur de compilation."
    exit 1
fi
