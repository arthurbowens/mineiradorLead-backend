# Build do JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests -B

# Runtime: imagem oficial Playwright Java — Chromium + deps já vêm na imagem.
# Evita baixar ~450MB na 1ª request (download longo + redeploy no meio = "Failed to read message").
# Versão alinhada ao pom (playwright.version).
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy
WORKDIR /app
COPY --from=build /app/target/lead-backend-0.1.0-SNAPSHOT.jar app.jar
# Não forçar PLAYWRIGHT_BROWSERS_PATH — usa os browsers embutidos na imagem.

EXPOSE 8080
ENV PORT=8080
# Imagem já define usuário adequado para Playwright; não precisa HOME=/app manual.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
