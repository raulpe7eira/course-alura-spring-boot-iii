FROM openjdk:8-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} monitor.jar
ENTRYPOINT ["java", "-Xmx512m", "-Dserver.port=${SERVER_PORT}", "-jar", "/monitor.jar"]
