# 1. Imagen base con JDK
FROM maven:3.9.6-eclipse-temurin-21

# 2. Directorio de la app
WORKDIR /app

# 3. Copiar pom y código fuente
COPY pom.xml .
COPY src ./src

# 4. Build del proyecto
RUN mvn clean package -DskipTests

# 5. Exponer puerto
EXPOSE 8080

# 6. Comando para ejecutar la app
ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]