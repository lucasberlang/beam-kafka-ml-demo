
FROM adoptopenjdk/maven-openjdk11:latest AS build

ADD . /app
WORKDIR /app
USER root
RUN mvn clean package -Dmaven.test.skip=true

FROM gcr.io/dataflow-templates-base/java11-template-launcher-base:latest

ARG WORKDIR=/template
ARG MAINCLASS=com.bluetab.application.KafkaConsumerApp
RUN mkdir -p ${WORKDIR}
WORKDIR ${WORKDIR}

COPY --from=build /app/target/kafka-consumer-1.0.0-SNAPSHOT.jar /template/
RUN mkdir -p ${WORKDIR}/lib
COPY --from=build /app/target/lib/ ${WORKDIR}/lib/

ENV FLEX_TEMPLATE_JAVA_CLASSPATH=/template/kafka-consumer-1.0.0-SNAPSHOT.jar
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS=${MAINCLASS}
