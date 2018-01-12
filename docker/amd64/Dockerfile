FROM openjdk:8-jre

VOLUME /tmp

ARG jar=app.jar
ADD $jar app.jar

#RUN sh -c 'touch /app.jar'

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]