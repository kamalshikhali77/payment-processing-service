FROM eclipse-temurin:11-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew clean assemble --no-daemon


FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=builder /app/build/libs/payment-processing-service-0.1.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]