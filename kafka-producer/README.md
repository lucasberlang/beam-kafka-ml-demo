# Kafka Producer

### Overview

This project is a Kafka producer implemented in Java, designed to efficiently publish data to Apache Kafka topics. Kafka is a distributed streaming platform that allows you to publish and subscribe to streams of records in real-time.

### Requirements

-   Java Development Kit (JDK) version 8 or higher
-   Apache Maven (for building the project)
-   Apache Kafka installed and running in your local

### Create Kafka Application

In this demo, we'll create a simple java Kafka application that has 1 producer. The producer periodically sends a random event
to a Kafka topic.


Open a new terminal and run the following commands.

-   Set constant variables

    ```shell
    export TOPIC="my-topic"
    ```

-   Run the producer to begin sending messages to the topic

    ```shell
    mvn clean install
    mvn exec:java \
      -Dexec.mainClass="org.kafka.SimpleKafkaProducer" \
      -Dexec.args="$TOPIC"

    ```
    Output logs should look similar to

    ```
    ...
    Message sent: {"amount":307,"latitude":-55.365952,"transaction":"bdd5fee5-60ef-4efd-84cc-2f2a67a95227","latLng":"(-55.365952,38.236547)","longitude":38.236547,"timestamp":"2024-02-15 18:54:25"}
    Message sent: {"amount":285,"latitude":-42.358339,"transaction":"77c56c54-1cb2-4643-b7ca-cd1068e7d562","latLng":"(-42.358339,90.062827)","longitude":90.062827,"timestamp":"2024-02-15 18:54:27"}
    Message sent: {"amount":404,"latitude":52.290886,"transaction":"fe523ff3-0436-4b67-8f16-479200a2b3fe","latLng":"(52.290886,-44.808988)","longitude":-44.808988,"timestamp":"2024-02-15 18:54:28"}
    ...
    ```
