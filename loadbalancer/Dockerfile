FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

COPY src ./src

RUN javac -d out $(find src -name "*.java")

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /build/out ./out


CMD ["java", "-cp", "out", "Main"]