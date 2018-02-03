FROM openjdk:jre-slim

VOLUME /tmp

RUN curl -L -s -o /app.jar https://github.com/SimpleCollector/simplecollector/releases/download/latest-build/simplecollector-latest.jar \
 && mkdir -p html && curl -L -s https://github.com/SimpleCollector/simplecollector-gui/releases/download/latest-build/simplecollector-gui-dist.tgz | tar xvz -C html

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.resources.static-locations=file:/html/", "-jar","/app.jar"]