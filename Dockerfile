FROM openjdk:8-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV profile dev
ENV clientId overrideMe
ENV clientSecret overrideMe
ENV dbUser overrideMe
ENV dbPass overrideMe
# TODO: is this logged somewhere?? More secure way to pass the secrets from k8s/cloudbuild?
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${profile}", \
"-Dspring.security.oauth2.client.registration.google.client-id=${clientId}", \
"-Dspring.security.oauth2.client.registration.google.client-secret=${clientSecret}", \
"-Dspring.datasource.username=${dbUser}", \
"-Dspring.datasource.password=${dbPass}", \
"-cp","app:app/lib/*","org.informiz.InformizWebApp"]
