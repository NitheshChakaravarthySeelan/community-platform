FROM maven:3.9.1-eclipse-temurin-17 as build
WORKDIR /app
COPY services/pricing/tax-calculation/pom.xml ./pom.xml
COPY services/pricing/tax-calculation/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package -Dmaven.wagon.http.readTimeout=120000 -Dmaven.wagon.http.retryHandler.count=5

FROM eclipse-temurin:17-jre
# Create a non-root user and group
RUN groupadd --system springapp && useradd --system --uid 1001 --gid springapp springapp
USER springapp

COPY --from=build --chown=springapp:springapp /app/target/tax-calculation-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]