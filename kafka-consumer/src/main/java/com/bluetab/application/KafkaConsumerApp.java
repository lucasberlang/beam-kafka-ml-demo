package com.bluetab.application;

import java.util.Collections;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import autovalue.shaded.com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryInsertError;
import org.apache.beam.sdk.io.gcp.bigquery.InsertRetryPolicy;
import org.apache.beam.sdk.io.gcp.bigquery.WriteResult;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.joda.time.Instant;

import com.bluetab.domain.Events;
import com.bluetab.coders.FailsafeElementCoder;
import com.bluetab.values.FailsafeElement;
import com.bluetab.utils.BigQueryIOUtils;
import com.bluetab.utils.SchemaErrorUtils;
import com.bluetab.common.ErrorConverters;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.google.gson.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerApp {

        private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerApp.class);
        private static final Gson GSON = new Gson();
        private static final String DEFAULT_DEADLETTER_TABLE_SUFFIX = "_error_records";

        private static final FailsafeElementCoder<String, String> FAILSAFE_ELEMENT_CODER = FailsafeElementCoder.of(
                        NullableCoder.of(StringUtf8Coder.of()), NullableCoder.of(StringUtf8Coder.of()));

        public static void main(String[] args) throws Exception {

                final KafkaConsumerOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                                .as(KafkaConsumerOptions.class);

                final String url = options.getUrlEndpoint();
                final String token = options.getGoogleAccessToken();
                LOG.warn("Starting job...  " + options.getJobName());
                String consumerGroupId = "consumer-group-df";

                LOG.warn("Running Pipeline with parameter vertex endpoint: {}" + url);
                LOG.warn("Running Pipeline with parameter kafka botstrap server: {}" + options.getKafkaServer());

                Pipeline pipeline = Pipeline.create(options);

                LOG.warn("Reading from Kafka: " + options.getKafkaServer() + " with Consumer group: "
                                + consumerGroupId);

                PCollection<String> genericEvents = pipeline
                                .apply("Read from Kafka", KafkaIO.<String, String>read()
                                                .withKeyDeserializer(StringDeserializer.class)
                                                .withValueDeserializer(StringDeserializer.class)
                                                .withBootstrapServers(options.getKafkaServer())
                                                .withConsumerConfigUpdates(
                                                                ImmutableMap.of("auto.offset.reset", "earliest"))
                                                .withConsumerConfigUpdates(ImmutableMap.of("group.id", consumerGroupId))
                                                .withTopics(Collections.singletonList(options.getInputTopic()))
                                                .commitOffsetsInFinalize()
                                                .withoutMetadata())
                                .apply("Calculated if transaction its fraud",
                                                ParDo.of(new DoFn<KV<String, String>, KV<String, String>>() {
                                                        @ProcessElement
                                                        public void processElement(ProcessContext c) {
                                                                KV<String, String> record = c.element();
                                                                String value = record.getValue();
                                                                JsonObject jsonObject = JsonParser.parseString(value)
                                                                                .getAsJsonObject();
                                                                JsonArray instancesArray = new JsonArray();
                                                                JsonObject filteredJsonObjectArray = new JsonObject();
                                                                filterKeys(jsonObject, instancesArray);
                                                                JsonArray instances2DArray = new JsonArray();
                                                                instances2DArray.add(instancesArray);

                                                                filteredJsonObjectArray.add("instances",
                                                                                instances2DArray);

                                                                String jsonString = new Gson()
                                                                                .toJson(filteredJsonObjectArray);

                                                                double prediction = sendHttpPostRequest(url, jsonString,
                                                                                token);
                                                                jsonObject.addProperty("score", prediction);
                                                                String updatedJsonString = new Gson()
                                                                                .toJson(jsonObject);

                                                                c.output(KV.of(record.getKey(), updatedJsonString));
                                                        }
                                                }))
                                .apply("Extract Payload", Values.<String>create());

                LOG.warn("Transform events to TableRow");

                PCollection<TableRow> eventsTableRows = genericEvents
                                .apply("Parse JSON", MapElements.into(TypeDescriptor.of(Events.class))
                                                .via(message -> GSON.fromJson(message, Events.class)))
                                .apply("Fixed-size windows",
                                                Window.into(FixedWindows.of(Duration
                                                                .standardMinutes(options.getWindowsDuration()))))
                                .apply("Convert to BigQuery TableRow", MapElements
                                                .into(TypeDescriptor.of(TableRow.class))
                                                .via(pageRating -> new TableRow()
                                                                .set("processing_time",
                                                                                new Instant(Instant.now()).toString())
                                                                .set("step", pageRating.getStep())
                                                                .set("score", pageRating.getScore())
                                                                .set("type", pageRating.getType())
                                                                .set("amount", pageRating.getAmount())
                                                                .set("nameOrig", pageRating.getNameOrig())
                                                                .set("oldBalanceOrig", pageRating.getOldBalanceOrig())
                                                                .set("newBalanceOrig", pageRating.getNewBalanceOrig())
                                                                .set("nameDest", pageRating.getNameDest())
                                                                .set("oldBalanceDest", pageRating.getOldBalanceDest())
                                                                .set("newBalanceDest", pageRating.getNewBalanceDest())
                                                                .set("latitude", pageRating.getLatitude())
                                                                .set("longitude", pageRating.getLongitude())
                                                                .set("lat_lng", String.format("POINT(%s %s)",
                                                                                pageRating.getLongitude(),
                                                                                pageRating.getLatitude()))
                                                                .set("timestamp", pageRating.getTimestamp())));

                WriteResult writeResult;

                writeResult = eventsTableRows.apply("Write to BigQuery",
                                BigQueryIO.writeTableRows()
                                                .to(options.getOutputTable())
                                                .withSchema(new TableSchema().setFields(Arrays.asList(
                                                                new TableFieldSchema().setName("processing_time")
                                                                                .setType("TIMESTAMP"),
                                                                new TableFieldSchema().setName("step").setType("INT64"),
                                                                new TableFieldSchema().setName("type")
                                                                                .setType("STRING"),
                                                                new TableFieldSchema().setName("amount")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("nameOrig")
                                                                                .setType("String"),
                                                                new TableFieldSchema().setName("oldBalanceOrig")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("newBalanceOrig")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("nameDest")
                                                                                .setType("String"),
                                                                new TableFieldSchema().setName("oldBalanceDest")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("newBalanceDest")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("latitude")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("longitude")
                                                                                .setType("FLOAT"),
                                                                new TableFieldSchema().setName("lat_lng")
                                                                                .setType("GEOGRAPHY"),
                                                                new TableFieldSchema().setName("timestamp")
                                                                                .setType("TIMESTAMP"),
                                                                new TableFieldSchema().setName("score")
                                                                                .setType("FLOAT"))))
                                                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                                                .withWriteDisposition(WriteDisposition.WRITE_APPEND)
                                                .withoutValidation()
                                                .withExtendedErrorInfo()
                                                .withFailedInsertRetryPolicy(InsertRetryPolicy.retryTransientErrors()));

                PCollection<FailsafeElement<String, String>> failedInserts = BigQueryIOUtils
                                .writeResultToBigQueryInsertErrors(writeResult, options)
                                .apply(
                                                "WrapInsertionErrors",
                                                MapElements.into(FAILSAFE_ELEMENT_CODER.getEncodedTypeDescriptor())
                                                                .via(KafkaConsumerApp::wrapBigQueryInsertError))
                                .setCoder(FAILSAFE_ELEMENT_CODER);

                LOG.warn("Writting error data in Bigquery: " + options.getOutputTable()
                                + DEFAULT_DEADLETTER_TABLE_SUFFIX);

                failedInserts.apply("FailedRecordToTableRow", ParDo.of(new ErrorConverters.FailedStringToTableRowFn()))
                                .apply(
                                                "WriteFailedRecordsToBigQuery",
                                                BigQueryIO.writeTableRows()
                                                                .to(options.getOutputTable()
                                                                                + DEFAULT_DEADLETTER_TABLE_SUFFIX)
                                                                .withJsonSchema(SchemaErrorUtils.DEADLETTER_SCHEMA)
                                                                .withCreateDisposition(
                                                                                CreateDisposition.CREATE_IF_NEEDED)
                                                                .withWriteDisposition(WriteDisposition.WRITE_APPEND));

                PipelineResult pipelineResult = pipeline.run();
                try {
                        pipelineResult.getState();
                        pipelineResult.waitUntilFinish(Duration.standardSeconds(options.getDuration()));
                } catch (UnsupportedOperationException e) {
                } catch (Exception e) {
                        LOG.error("The error of the application is: " + e);
                        e.printStackTrace();
                }
        }

        protected static FailsafeElement<String, String> wrapBigQueryInsertError(
                        BigQueryInsertError insertError) {

                FailsafeElement<String, String> failsafeElement;
                try {

                        failsafeElement = FailsafeElement.of(
                                        insertError.getRow().toPrettyString(), insertError.getRow().toPrettyString());
                        failsafeElement.setErrorMessage(insertError.getError().toPrettyString());

                } catch (IOException e) {
                        LOG.error("Failed to wrap BigQuery insert error.");
                        throw new RuntimeException(e);
                }
                return failsafeElement;
        }

        private static void filterKeys(JsonObject jsonObject, JsonArray instancesArray) {
                String[] keys = { "step", "amount", "oldBalanceOrig", "newBalanceOrig", "oldBalanceDest",
                                "newBalanceDest" };
                for (String key : keys) {
                        instancesArray.add(jsonObject.get(key));
                }

                String[] types = { "CASH_IN", "CASH_OUT", "DEBIT", "PAYMENT", "TRANSFER" };
                int[][] typeIndices = { { 1, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0 }, { 0, 0, 1, 0, 0 }, { 0, 0, 0, 1, 0 },
                                { 0, 0, 0, 0, 1 } };

                String type = jsonObject.get("type").getAsString();

                int typeIndex = -1;
                for (int i = 0; i < types.length; i++) {
                        if (types[i].equals(type)) {
                                typeIndex = i;
                                break;
                        }
                }

                if (typeIndex != -1) {
                        for (int value : typeIndices[typeIndex]) {
                                instancesArray.add(value);
                        }
                }
        }

        private static double sendHttpPostRequest(String url, String json, String googleAccessToken) {
                try {
                        CloseableHttpClient httpClient = HttpClients.createDefault();

                        HttpPost httpPost = new HttpPost(url);

                        StringEntity entity = new StringEntity(json);
                        httpPost.setEntity(entity);
                        httpPost.setHeader("Content-Type", "application/json");
                        httpPost.setHeader("Authorization",
                                        "Bearer " + googleAccessToken);

                        CloseableHttpResponse response = httpClient.execute(httpPost);
                        String responseBody = new BufferedReader(
                                        new InputStreamReader(response.getEntity().getContent()))
                                        .lines().collect(Collectors.joining("\n"));
                        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                        JsonArray predictionsArray = jsonObject.getAsJsonArray("predictions");

                        double prediction = predictionsArray.get(0).getAsDouble();

                        if (Math.abs(prediction) < 0.001) {
                                return 0.0;
                        }

                        response.close();
                        httpClient.close();
                        return prediction;
                } catch (Exception e) {
                        e.printStackTrace();
                        return Double.NaN;
                }
        }

}
