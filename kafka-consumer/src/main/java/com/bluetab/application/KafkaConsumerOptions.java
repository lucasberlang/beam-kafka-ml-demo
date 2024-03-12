package com.bluetab.application;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

public interface KafkaConsumerOptions extends BigQueryStorageApiStreamingOptions {
    @Description("Kafka Bootstrap Servers")
    @Default.String("localhost:29092")
    String getKafkaServer();

    void setKafkaServer(String value);

    @Description("Kafka Topic Name")
    @Default.String("go-euw4-ing-blue-data-sink-topic01-tst")
    String getInputTopic();

    void setInputTopic(String value);

    @Description("Bigquery Output Table in format {project_id}:{dataset}.{table}")
    @Default.String("practica-cloud-286009:kafka_streaming_dataflow_euw4_ds01.kafka-sink-table")
    String getOutputTable();

    void setOutputTable(String value);

    @Description("Bigquery Schema Table")
    @Default.String("transaction:STRING,amount:FLOAT,latitude:FLOAT,longitude:FLOAT,timestamp:TIMESTAMP")
    String getSchemaTable();

    void setSchemaTable(String value);

    @Description("Pipeline duration to wait until finish in seconds")
    @Default.Long(-1)
    Long getDuration();

    void setDuration(Long duration);

    @Description("Windows interval in seconds for grouping incoming messages.")
    @Default.Long(60)
    Long getWindowsDuration();

    void setWindowsDuration(Long duration);


    @Description("Endpoint the vertex")
    @Default.String("https://europe-west4-aiplatform.googleapis.com/v1/projects/827851015670/locations/europe-west4/endpoints/3701312380861415424:predict")
    String getUrlEndpoint();

    void setUrlEndpoint(String value);

    @Description("Google Access Token")
    @Default.String("xxx")
    String getGoogleAccessToken();

    void setGoogleAccessToken(String value);


}
