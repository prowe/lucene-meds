FROM openjdk:17-jdk-alpine AS build

RUN apk add curl unzip

WORKDIR /work

COPY . .
RUN ./build-index-file.sh
RUN ./mvnw package

FROM openjdk:17-jdk-alpine

EXPOSE 8080

COPY --from=build /work/target/lucine-meds-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /work/data data

ENTRYPOINT ["java","-jar","/app.jar"]