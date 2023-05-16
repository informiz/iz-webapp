# TODO: move to containerd
FROM debian:11-slim

ARG DEBIAN_FRONTEND=noninteractive
ARG DEBCONF_NOWARNINGS="yes"

# Install java-17
RUN mkdir -p /usr/share/man/man1 /usr/share/man/man2
RUN apt-get update
RUN apt-get install -y --no-install-recommends openjdk-17-jre

# Create group and user for spring
# [equiv. command for Alpine] RUN addgroup --gid 22222 spring && adduser spring --gecos "" --disabled-password --ingroup spring
RUN groupadd -g 22222 spring && useradd -g spring 22222
USER 22222:spring

# Web-app
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# Override default-profile on K8S
ENV profile test
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${profile}", \
"-cp","app:app/lib/*","org.informiz.InformizWebApp"]
