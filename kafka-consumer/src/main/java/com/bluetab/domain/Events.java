package com.bluetab.domain;

import java.io.Serializable;
import org.apache.avro.reflect.Nullable;

import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import lombok.*;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
@DefaultCoder(AvroCoder.class)
public class Events implements Serializable {
    @Nullable
    String step;
    @Nullable
    String score;
    @Nullable
    String type;
    @Nullable
    String amount;
    @Nullable
    String nameOrig;
    @Nullable
    String oldBalanceOrig;
    @Nullable
    String newBalanceOrig;
    @Nullable
    String nameDest;
    @Nullable
    String oldBalanceDest;
    @Nullable
    String newBalanceDest;
    @Nullable
    String latitude;
    @Nullable
    String longitude;
    @Nullable
    String timestamp;
}
