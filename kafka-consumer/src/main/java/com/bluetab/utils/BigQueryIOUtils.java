package com.bluetab.utils;

import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse.InsertErrors;
import com.google.api.services.bigquery.model.TableReference;
import java.util.List;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryInsertError;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryInsertErrorCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryOptions;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryStorageApiInsertError;
import org.apache.beam.sdk.io.gcp.bigquery.WriteResult;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;

public final class BigQueryIOUtils {
    private static final TableReference EMPTY_TABLE_REFERENCE =
        new TableReference().setTableId("unknown").setProjectId("unknown").setDatasetId("unknown");

    private BigQueryIOUtils() {}

    public static void validateBQStorageApiOptionsBatch(BigQueryOptions options) {
      validateStorageWriteApiAtLeastOnce(options);
    }

    public static void validateBQStorageApiOptionsStreaming(BigQueryOptions options) {
      if (options.getUseStorageWriteApi()
          && !options.getUseStorageWriteApiAtLeastOnce()
          && (options.getNumStorageWriteApiStreams() < 1
              || options.getStorageWriteApiTriggeringFrequencySec() == null)) {
        // the number of streams and triggering frequency are only required for exactly-once semantics
        throw new IllegalArgumentException(
            "When streaming with STORAGE_WRITE_API, the number of streams"
                + " (numStorageWriteApiStreams) and triggering frequency"
                + " (storageWriteApiTriggeringFrequencySec) must also be specified.");
      }
      StreamingModeUtils.validateBQOptions(options.as(DataflowPipelineOptions.class));
      validateStorageWriteApiAtLeastOnce(options);
    }

    /** Converts a {@link BigQueryStorageApiInsertError} into a {@link BigQueryInsertError}. */
    public static PCollection<BigQueryInsertError> writeResultToBigQueryInsertErrors(
        WriteResult writeResult, BigQueryOptions options) {
      if (options.getUseStorageWriteApi()) {
        return writeResult
            .getFailedStorageApiInserts()
            .apply(
                MapElements.via(
                    new SimpleFunction<BigQueryStorageApiInsertError, BigQueryInsertError>() {
                      public BigQueryInsertError apply(BigQueryStorageApiInsertError error) {
                        return new BigQueryInsertError(
                            error.getRow(),
                            new InsertErrors()
                                .setErrors(
                                    List.of(new ErrorProto().setMessage(error.getErrorMessage()))),
                            EMPTY_TABLE_REFERENCE);
                      }
                    }))
            .setCoder(BigQueryInsertErrorCoder.of());
      }
      return writeResult.getFailedInsertsWithErr();
    }

    private static void validateStorageWriteApiAtLeastOnce(BigQueryOptions options) {
      if (options.getUseStorageWriteApiAtLeastOnce() && !options.getUseStorageWriteApi()) {
        // Technically this is a no-op, since useStorageWriteApiAtLeastOnce is only checked by
        // BigQueryIO when useStorageWriteApi is true, but it might be confusing to a user why
        // useStorageWriteApiAtLeastOnce doesn't take effect.
        throw new IllegalArgumentException(
            "When at-least-once semantics (useStorageWriteApiAtLeastOnce) are enabled Storage Write"
                + " API (useStorageWriteApi) must also be enabled.");
      }
    }
  }
