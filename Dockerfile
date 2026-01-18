FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src src

RUN mvn package

FROM eclipse-temurin:21-alpine

WORKDIR /app

COPY --from=builder /app/target/WiiLink-Bot.jar .

CMD ["java", "-jar", "WiiLink-Bot.jar"]