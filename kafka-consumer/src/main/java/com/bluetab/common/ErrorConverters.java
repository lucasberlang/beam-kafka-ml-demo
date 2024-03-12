
package com.bluetab.common;

import com.google.api.services.bigquery.model.TableRow;
import com.bluetab.values.FailsafeElement;
import java.nio.charset.StandardCharsets;
import org.apache.beam.sdk.transforms.DoFn;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ErrorConverters {

  /**
   * The {@link FailedStringToTableRowFn} converts string objects which have failed processing into
   * {@link TableRow} objects which can be output to a dead-letter table.
   */
  public static class FailedStringToTableRowFn
      extends DoFn<FailsafeElement<String, String>, TableRow> {

    /**
     * The formatter used to convert timestamps into a BigQuery compatible <a
     * href="https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#timestamp-type">format</a>.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @ProcessElement
    public void processElement(ProcessContext context) {
      FailsafeElement<String, String> failsafeElement = context.element();
      final String message = failsafeElement.getOriginalPayload();

      // Format the timestamp for insertion
      String timestamp =
          TIMESTAMP_FORMATTER.print(context.timestamp().toDateTime(DateTimeZone.UTC));

      // Build the table row
      final TableRow failedRow =
          new TableRow()
              .set("timestamp", timestamp)
              .set("errorMessage", failsafeElement.getErrorMessage())
              .set("stacktrace", failsafeElement.getStacktrace());

      // Only set the payload if it's populated on the message.
      if (message != null) {
        failedRow
            .set("payloadString", message)
            .set("payloadBytes", message.getBytes(StandardCharsets.UTF_8));
      }

      context.output(failedRow);
    }
  }

}
