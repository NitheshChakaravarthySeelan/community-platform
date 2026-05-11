FROM maven:3.9.1-eclipse-temurin-17 as build
WORKDIR /app
COPY shared/proto /shared/proto
COPY services/orders/order-read/pom.xml ./
COPY services/orders/order-read/src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]