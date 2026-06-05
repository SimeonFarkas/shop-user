# STEG 1: Bygg applikationen med Maven och Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiera inställningsfiler och källkod
COPY pom.xml .
COPY src ./src

# Kompilera koden och bygg JAR-filen (vi hoppar över testerna just nu)
RUN mvn clean package -DskipTests

# STEG 2: Skapa den lätta containern för att köra appen med Java 17 JRE
FROM eclipse-temurin:17-jre
WORKDIR /app

# Kopiera den färdiga JAR-filen från Steg 1
COPY --from=build /app/target/*.jar app.jar

# Berätta för Docker vilken port appen lyssnar på (t.ex. 8080 för Spring Boot)
EXPOSE 8080

# Kommandot för att starta din Java-applikation
CMD ["java", "-jar", "app.jar"]