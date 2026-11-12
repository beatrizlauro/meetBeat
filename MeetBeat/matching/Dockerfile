# Imagem usada para publicar a API na nuvem (Render). Localmente nada muda: continue rodando pelo VS Code ou mvnw.

# 1) Compila o .jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# 2) Imagem final, só com o Java e o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
# Imagens da base fictícia (geradas por seed/popular_banco.py), servidas em /uploads/seed/...
COPY uploads/seed ./uploads/seed
# O plano gratuito tem 512 MB de RAM: limita a memória da JVM para caber com folga
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
