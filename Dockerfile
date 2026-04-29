FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Tests are run in CI before image build; skip here to keep the image build fast.
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user. The jre image doesn't include one, so create it explicitly.
RUN groupadd --system --gid 1001 app \
 && useradd  --system --uid 1001 --gid app --home-dir /app --shell /usr/sbin/nologin app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
