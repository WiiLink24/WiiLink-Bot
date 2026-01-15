FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src src

RUN mvn package

CMD ["java", "-jar", "target/WiiLink-Bot.jar"]