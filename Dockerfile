# TODO: move to containerd
FROM adoptopenjdk/openjdk11:debian-slim

RUN groupadd spring && useradd -g spring spring
USER spring:spring

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV profile test
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${profile}", \
"-cp","app:app/lib/*","org.informiz.InformizWebApp"]
