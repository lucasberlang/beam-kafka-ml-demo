package com.bluetab.utils;

import static java.util.stream.Collectors.toList;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.common.base.Strings;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import java.util.List;
import javax.annotation.Nullable;

public class SchemaErrorUtils {

  private static final int MAX_BIG_QUERY_RECORD_NESTING = 15;

  public static final String DEADLETTER_SCHEMA =
      "{\n"
          + "  \"fields\": [\n"
          + "    {\n"
          + "      \"name\": \"timestamp\",\n"
          + "      \"type\": \"TIMESTAMP\",\n"
          + "      \"mode\": \"REQUIRED\"\n"
          + "    },\n"
          + "    {\n"
          + "      \"name\": \"payloadString\",\n"
          + "      \"type\": \"STRING\",\n"
          + "      \"mode\": \"REQUIRED\"\n"
          + "    },\n"
          + "    {\n"
          + "      \"name\": \"payloadBytes\",\n"
          + "      \"type\": \"BYTES\",\n"
          + "      \"mode\": \"REQUIRED\"\n"
          + "    },\n"
          + "    {\n"
          + "      \"name\": \"attributes\",\n"
          + "      \"type\": \"RECORD\",\n"
          + "      \"mode\": \"REPEATED\",\n"
          + "      \"fields\": [\n"
          + "        {\n"
          + "          \"name\": \"key\",\n"
          + "          \"type\": \"STRING\",\n"
          + "          \"mode\": \"NULLABLE\"\n"
          + "        },\n"
          + "        {\n"
          + "          \"name\": \"value\",\n"
          + "          \"type\": \"STRING\",\n"
          + "          \"mode\": \"NULLABLE\"\n"
          + "        }\n"
          + "      ]\n"
          + "    },\n"
          + "    {\n"
          + "      \"name\": \"errorMessage\",\n"
          + "      \"type\": \"STRING\",\n"
          + "      \"mode\": \"NULLABLE\"\n"
          + "    },\n"
          + "    {\n"
          + "      \"name\": \"stacktrace\",\n"
          + "      \"type\": \"STRING\",\n"
          + "      \"mode\": \"NULLABLE\"\n"
          + "    }\n"
          + "  ]\n"
          + "}";

  /**
   * Infers the proper {@link TableSchema} for a Protobuf message.
   *
   * <p>The name value for each field in the returned schema will default to {@link
   * FieldDescriptor#getJsonName()}. If this value is not present, then it will use {@link
   * FieldDescriptor#getName()}.
   *
   * <p>Type mappings follow the rules:
   *
   * <ul>
   *   <li>Integers and longs map to {@link LegacySQLTypeName#INTEGER}.
   *   <li>Floats and doubles map to {@link LegacySQLTypeName#FLOAT}.
   *   <li>Booleans map to {@link LegacySQLTypeName#BOOLEAN}.
   *   <li>Strings and enums map to {@link LegacySQLTypeName#STRING}.
   *   <li>Byte strings map to {@link LegacySQLTypeName#BYTES}.
   *   <li>Messages and maps map to {@link LegacySQLTypeName#RECORD}.
   * </ul>
   *
   * <p>Fields marked as `oneof` in the proto definition will be expanded out into individual
   * fields. `oneof` fields are incompatible with BigQuery otherwise.
   *
   * <p>An error will be thrown if excessive RECORD nesting is detected. BigQuery has more
   * restrictive RECORD nesting limits than Protobuf has message nesting. Circular message
   * references and self-referential messages are not supported for this reason.
   *
   * <p>Proto repeated fields will be marked as REPEATED in BigQuery. Required and optional fields
   * will be marked as REQUIRED and NULLABLE respectively.
   *
   * <p>No description or policy tags will be set for any of the fields.
   *
   * @param descriptor a proto {@link Descriptor} to be converted into a BigQuery schema
   * @param preserveProtoFieldNames true to keep proto snake_case. False to use lowerCamelCase. If
   *     set to false and {@link FieldDescriptor#getJsonName()} is not set, then snake_case will be
   *     used.
   * @return a full BigQuery schema definition
   */
  public static TableSchema createBigQuerySchema(
      Descriptor descriptor, boolean preserveProtoFieldNames) {
    // TableSchema and TableFieldSchema work better with Beam than Schema and Field.
    List<TableFieldSchema> fields =
        descriptor.getFields().stream()
            .map(
                fd ->
                    convertProtoFieldDescriptorToBigQueryField(
                        fd, preserveProtoFieldNames, /* parent= */ null, /* nestingLevel= */ 1))
            .collect(toList());
    TableSchema schema = new TableSchema();
    schema.setFields(fields);
    return schema;
  }

  /** Handlers proto field to BigQuery field conversion. */
  public static TableFieldSchema convertProtoFieldDescriptorToBigQueryField(
      FieldDescriptor fieldDescriptor,
      boolean preserveProtoFieldNames,
      @Nullable FieldDescriptor parent,
      int nestingLevel) {
    TableFieldSchema schema = new TableFieldSchema();

    String jsonName = fieldDescriptor.getJsonName();
    schema.setName(
        preserveProtoFieldNames || Strings.isNullOrEmpty(jsonName)
            ? fieldDescriptor.getName()
            : jsonName);

    LegacySQLTypeName sqlType = convertProtoTypeToSqlType(fieldDescriptor.getJavaType());
    schema.setType(sqlType.toString());

    if (sqlType == LegacySQLTypeName.RECORD) {
      if (nestingLevel > MAX_BIG_QUERY_RECORD_NESTING) {
        throw new IllegalArgumentException(
            String.format(
                "Record field `%s.%s` is at BigQuery's nesting limit of %s, but it contains"
                    + " message field `%s` of type `%s`. This could be caused by circular message"
                    + " references, including a self-referential message.",
                parent.getMessageType().getName(),
                parent.getName(),
                MAX_BIG_QUERY_RECORD_NESTING,
                fieldDescriptor.getName(),
                fieldDescriptor.getMessageType().getName()));
      }

      List<TableFieldSchema> subFields =
          fieldDescriptor.getMessageType().getFields().stream()
              .map(
                  fd ->
                      convertProtoFieldDescriptorToBigQueryField(
                          fd, preserveProtoFieldNames, fieldDescriptor, nestingLevel + 1))
              .collect(toList());
      schema.setFields(subFields);
    }

    if (fieldDescriptor.isRepeated()) {
      schema.setMode("REPEATED");
    } else if (fieldDescriptor.isRequired()) {
      schema.setMode("REQUIRED");
    } else {
      schema.setMode("NULLABLE");
    }

    return schema;
  }

  /** Handles mapping a proto type to BigQuery type. */
  private static LegacySQLTypeName convertProtoTypeToSqlType(JavaType protoType) {
    switch (protoType) {
      case INT:
        // fall through
      case LONG:
        return LegacySQLTypeName.INTEGER;
      case FLOAT:
        // fall through
      case DOUBLE:
        return LegacySQLTypeName.FLOAT;
      case BOOLEAN:
        return LegacySQLTypeName.BOOLEAN;
      case ENUM:
        // fall through
      case STRING:
        return LegacySQLTypeName.STRING;
      case BYTE_STRING:
        return LegacySQLTypeName.BYTES;
      case MESSAGE:
        return LegacySQLTypeName.RECORD;
    }
    throw new IllegalArgumentException(String.format("Unrecognized type: %s", protoType));
  }
}
