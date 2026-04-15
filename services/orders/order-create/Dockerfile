FROM maven:3.9.1-eclipse-temurin-17 as build
WORKDIR /shared/libs/java/kafka-dtos
COPY shared/libs/java/kafka-dtos ./
RUN mvn install -DskipTests

WORKDIR /app
COPY shared/proto /shared/proto
COPY services/orders/order-create/pom.xml ./
COPY services/orders/order-create/src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]