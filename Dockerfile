# For Java 11, try this
# FROM openjdk:11-jdk-alpine
FROM adoptopenjdk/openjdk11:alpine-jre

# cd /opt/app
WORKDIR /opt/app

# Refer to Maven build -> finalName
ARG JAR_FILE=target/localFileSystemProject-0.0.1-SNAPSHOT.jar

# cp target/spring-boot-web.jar /opt/app/fileSystem_v1.jar
COPY ${JAR_FILE} fileSystem_v1.jar

# java -jar /opt/app/fileSystem_v1.jar
ENTRYPOINT ["java","-jar","fileSystem_v1.jar"]