FROM gradle:7.0.2-jdk16 AS builder

USER root
WORKDIR /home/gradle
COPY . .
RUN gradle bootJar --stacktrace

FROM adoptopenjdk/openjdk16:debianslim-jre
COPY --from=builder /home/gradle/build/libs/*.jar board.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=release", "-Dspring.data.mongodb.host=${MONGODB_HOST}", "-Dspring.data.mongodb.port=${MONGODB_PORT}", "-Dspring.data.mongodb.database=${MONGODB_DATABASE}", "-Dspring.data.mongodb.username=${MONGODB_USERNAME}", "-Dspring.data.mongodb.password=${MONGODB_PASSWORD}", "./board.jar"]