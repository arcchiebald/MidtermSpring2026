FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && mvn -q -B package -DskipTests \
    && apt-get purge -y maven \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/uno-cli-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
