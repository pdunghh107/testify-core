FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY . .
RUN mvn -B -ntp -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/auth-service/target/auth-service-*.jar /app/app.jar
EXPOSE 9001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
