# TODO: official linux+java8 image that does not conflict with IBM/Spring grpc and netty libs?
FROM rtfpessoa/ubuntu-jdk8

RUN groupadd spring && useradd -g spring spring
USER spring:spring

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV profile test
ENV clientId overrideMe
ENV clientSecret overrideMe
ENV dbUser overrideMe
ENV dbPass overrideMe
# TODO: is this logged somewhere?? More secure way to pass the secrets from k8s/cloudbuild?
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${profile}", \
"-cp","app:app/lib/*","org.informiz.InformizWebApp"]
