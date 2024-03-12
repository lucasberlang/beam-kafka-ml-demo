# Kafka to BigQuery Pipeline with Apache Beam (Java)

## Overview

This Java application showcases a data pipeline that reads messages from a Kafka topic and writes them to Google BigQuery using Apache Beam. The pipeline is developed using the Apache Beam SDK for Java.

## Requirements

- **Java Development Kit (JDK) 8 or higher**
- **Apache Maven**
- **Google Cloud Platform (GCP) account with access to BigQuery**
- **Kafka cluster and topic**
- **Google Cloud Storage bucket (for temporary storage)**

## Environment Setup

1. Set up your Google Cloud environment:

    ```bash
    gcloud init
    export GOOGLE_APPLICATION_CREDENTIALS="/path/key.json"
    ```

2. Set up environment variables:

    ```bash
    export BUCKET="go-euw4-ing-blue-data-sink-cs01-tst"
    export TOPIC="go-euw4-ing-blue-data-sink-topic01-tst"
    export SUBSCRIPTION="pullsub-kafka"
    export PROJECT=emea-ing-p1146-tst
    export DATASET="test_streaming_dataflow_euw4_ds01"
    export BOOTSTRAP_SERVERS="10.142.0.193:9092"
    export TABLE="dummy_kafka_java_test"
    export REGION="europe-west4"
    export METADATA_FILE="config/metadata.json"
    export SUBNETWORK=regions/europe-west4/subnetworks/go-euw1-ing-blue-tst-mz-subnet01-pre
    export NETWORK=projects/emea-ing-p1146-tst/global/networks/go-euw4-ing-blue-data-mz-vpc01-tst
    ```
3. Build java app:

    ```bash
    mvn clean install
    ```

4. Build dataflow image:

    ```bash
    export TEMPLATE_IMAGE="gcr.io/$PROJECT/samples/dataflow/java-ing-streaming-beam:latest"
    gcloud builds submit --tag "$TEMPLATE_IMAGE" .
    ```

5. Build the Flex Template:

    ```bash
    export TEMPLATE_PATH="gs://$BUCKET/samples/dataflow/templates/java-ing-streaming-beam.json"

    gcloud dataflow flex-template build "$TEMPLATE_PATH" \
      --image "$TEMPLATE_IMAGE" \
      --sdk-language JAVA \
      --metadata-file "$METADATA_FILE"
    ```


6. Run the Flex Template on Kafka:


    ```bash
    gcloud dataflow flex-template run "kafka-java-streaming-beam-`date +%Y%m%d-%H%M%S`" \
        --template-file-gcs-location "$TEMPLATE_PATH" \
        --parameters inputTopic="$TOPIC" \
        --parameters kafkaServer="$BOOTSTRAP_SERVERS" \
        --parameters outputTable="$PROJECT:$DATASET.$TABLE" \
        --parameters ^~^schemaTable="message:STRING,timestamp:TIMESTAMP" \
        --region "$REGION" \
        --network "${NETWORK}" \
        --subnetwork "${SUBNETWORK}" \
        --disable-public-ips \
        --num-workers=2



        --additional-experiments=use_network_tags_for_flex_templates=dataflow
    ```

 ## Execute in local

    ```bash
    mvn exec:java -Dexec.mainClass=com.bluetab.application.KafkaConsumerApp -Dexec.args="'--kafkaServer=localhost:29092'"
    mvn exec:java -Dexec.mainClass=com.bluetab.application.KafkaConsumerApp -Dexec.args="'--kafkaServer=10.142.0.193:9092'"
    ```
