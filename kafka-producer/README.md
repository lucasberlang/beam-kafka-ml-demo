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
    Message sent: {"nameDest":"C931708965","amount":641283.99,"newBalanceOrig":6607597.069999999,"oldBalanceOrig":7248881.06,"newBalanceDest":7976840.78,"latitude":-47.057015,"step":716,"oldBalanceDest":7335556.79,"type":"DEBIT","nameOrig":"C526024005","longitude":169.086753,"timestamp":"2024-03-13 17:10:44"}
    Message sent: {"nameDest":"C280980768","amount":862637.36,"newBalanceOrig":7865130.83,"oldBalanceOrig":7002493.47,"newBalanceDest":0,"latitude":-19.644464,"step":492,"oldBalanceDest":344421.79,"type":"CASH_IN","nameOrig":"C374395053","longitude":104.18229,"timestamp":"2024-03-13 17:10:54"}
    Message sent: {"nameDest":"C89461874","amount":841680.05,"newBalanceOrig":2877499.9799999995,"oldBalanceOrig":3719180.03,"newBalanceDest":1649534.54,"latitude":-78.459997,"step":302,"oldBalanceDest":807854.49,"type":"TRANSFER","nameOrig":"C139677676","longitude":114.443676,"timestamp":"2024-03-13 17:11:04"}
    ...
    ```
