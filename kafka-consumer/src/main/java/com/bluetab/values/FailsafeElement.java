package com.bluetab.values;

import com.bluetab.coders.FailsafeElementCoder;
import java.util.Objects;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.vendor.guava.v32_1_2_jre.com.google.common.base.MoreObjects;

@DefaultCoder(FailsafeElementCoder.class)
public class FailsafeElement<OriginalT, CurrentT> {

  private final OriginalT originalPayload;
  private final CurrentT payload;
  @Nullable private String errorMessage;
  @Nullable private String stacktrace;

  private FailsafeElement(OriginalT originalPayload, CurrentT payload) {
    this.originalPayload = originalPayload;
    this.payload = payload;
  }

  public static <OriginalT, CurrentT> FailsafeElement<OriginalT, CurrentT> of(
      OriginalT originalPayload, CurrentT currentPayload) {
    return new FailsafeElement<>(originalPayload, currentPayload);
  }

  public static <OriginalT, CurrentT> FailsafeElement<OriginalT, CurrentT> of(
      FailsafeElement<OriginalT, CurrentT> other) {
    return new FailsafeElement<>(other.originalPayload, other.payload)
        .setErrorMessage(other.getErrorMessage())
        .setStacktrace(other.getStacktrace());
  }

  public OriginalT getOriginalPayload() {
    return originalPayload;
  }

  public CurrentT getPayload() {
    return payload;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public FailsafeElement<OriginalT, CurrentT> setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public FailsafeElement<OriginalT, CurrentT> setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final FailsafeElement other = (FailsafeElement) obj;
    return Objects.deepEquals(this.originalPayload, other.getOriginalPayload())
        && Objects.deepEquals(this.payload, other.getPayload())
        && Objects.deepEquals(this.errorMessage, other.getErrorMessage())
        && Objects.deepEquals(this.stacktrace, other.getStacktrace());
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalPayload, payload, errorMessage, stacktrace);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("originalPayload", originalPayload)
        .add("payload", payload)
        .add("errorMessage", errorMessage)
        .add("stacktrace", stacktrace)
        .toString();
  }
}
