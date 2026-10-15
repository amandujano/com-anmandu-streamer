# ==========================================
# ETAPA 1: Compilación de la aplicación (Builder)
# ==========================================
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app

# Copia los archivos de configuración de Maven o Gradle primero (para aprovechar la caché de capas)
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Descarga las dependencias (se cachea si el pom.xml no cambia)
RUN ./mvnw dependency:go-offline

# Copia el código fuente y compila el paquete JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================================
# ETAPA 2: Imagen final para ejecución
# ==========================================
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Crea un usuario sin privilegios por seguridad
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copia el archivo .jar generado en la etapa de construcción
# Cambia 'tu-aplicacion.jar' por el nombre real de tu jar o usa un comodín
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto en el que corre Spring Boot (por defecto 8080)
EXPOSE 8080

# Comando de inicio de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
