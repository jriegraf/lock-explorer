FROM openjdk:11
RUN addgroup spring && useradd -ms /bin/bash -g spring spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
