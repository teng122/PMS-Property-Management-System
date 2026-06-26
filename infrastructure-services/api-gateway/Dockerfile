# ==========================
# Stage 1: Build
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom trước để cache dependencies
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy source và Maven Wrapper
COPY . .

# Build
RUN mvn clean package -DskipTests

# ==========================
# Stage 2: Runtime
# ==========================
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache wget

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]