package com.bluetab.utils;


import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.ExperimentalOptions;

public class StreamingModeUtils {
  private StreamingModeUtils() {}

  public static void validate(DataflowPipelineOptions options) {
    if (ExperimentalOptions.hasExperiment(options, "streaming_mode_at_least_once")
        || (options.getDataflowServiceOptions() != null
            && options.getDataflowServiceOptions().contains("streaming_mode_at_least_once"))) {
      options.setEnableStreamingEngine(true);
    }
  }

  public static void validateBQOptions(DataflowPipelineOptions options) {
    validate(options);
    if (ExperimentalOptions.hasExperiment(options, "streaming_mode_at_least_once")
        || (options.getDataflowServiceOptions() != null
            && options.getDataflowServiceOptions().contains("streaming_mode_at_least_once"))) {
      options.setUseStorageWriteApi(true);
      options.setUseStorageWriteApiAtLeastOnce(true);
    }
  }
}
