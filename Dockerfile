# Utiliser une image OpenJDK 17 comme base
FROM eclipse-temurin:17-jdk-alpine

# Définir le dossier de travail
WORKDIR /app

# Copier le code source
COPY . .

# Rendre mvnw exécutable
RUN chmod +x ./mvnw

# Construire l’application
RUN ./mvnw clean package -DskipTests

# Lancer le jar
CMD ["java", "-jar", "target/Spring_jar_MTG-0.0.1-SNAPSHOT.jar"]
