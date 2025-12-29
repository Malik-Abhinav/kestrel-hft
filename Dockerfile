FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/install/demo /app
ENV REDIS_ENABLED=false
CMD ["bin/demo"]
