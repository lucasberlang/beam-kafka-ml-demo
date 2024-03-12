package org.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;

import com.javadocmd.simplelatlng.*;

public class SimpleKafkaProducer {
    public static void main(String[] args) {
        String topic;
        if (args.length == 0) {
            topic = "my-topic";
            System.out.println("No topic provided.");
        } else {
            topic = args[0];
        }

        // Set producer properties
        String bootstrapServer = "localhost:29092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        while (true) {
            double min = 0.0;
            double max = 1000000.0;
            double minBalance = 0.0;
            double maxBalance = 9999999.0;
            int minInt = 10000;
            int maxInt = 999999999;

            double randomAmount = getRandomDouble(min, max);
            String nameOrig = "C" + String.valueOf(getRandomInteger(minInt, maxInt));
            double oldBalanceOrig = getRandomDouble(minBalance, maxBalance);
            double oldBalanceDest = getRandomDouble(minBalance, maxBalance);
            LatLng randomLatLng = LatLng.random();
            double randomLat = randomLatLng.getLatitude();
            double randomLng = randomLatLng.getLongitude();
            String[] type = { "CASH_IN", "CASH_OUT", "DEBIT", "PAYMENT", "TRANSFER" };
            Integer step = getRandomInteger(1, 744);

            Random random = new Random();
            int randomIndex = random.nextInt(type.length);
            String randomType = type[randomIndex];
            String nameDest = getRandomNameDest(randomType) + String.valueOf(getRandomInteger(minInt, maxInt));

            double newBalanceOrig = getBalanceOrg(randomAmount, oldBalanceOrig, randomType);
            double newBalanceDest = getBalanceDest(randomAmount, oldBalanceDest, randomType);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("step", step);
            jsonObject.put("type", randomType);
            jsonObject.put("amount", randomAmount);
            jsonObject.put("nameOrig", nameOrig);
            jsonObject.put("oldBalanceOrig", oldBalanceOrig);
            jsonObject.put("newBalanceOrig", newBalanceOrig);
            jsonObject.put("nameDest", nameDest);
            jsonObject.put("oldBalanceDest", oldBalanceDest);
            jsonObject.put("newBalanceDest", newBalanceDest);
            jsonObject.put("latitude", randomLat);
            jsonObject.put("longitude", randomLng);
            jsonObject.put("timestamp", formattedTime);

            String message = jsonObject.toString();
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            producer.send(record);

            System.out.println("Message sent: " + message);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static double getBalanceOrg(double amount, double oldBalance, String type) {
        double newBalance;

        if (type == "CASH_IN") {
            newBalance = oldBalance + amount;
        } else {
            newBalance = oldBalance - amount;
            if (newBalance < 0.0) {
                newBalance = 0.0;
            }
        }
        return newBalance;
    }

    public static double getBalanceDest(double amount, double oldBalance, String type) {
        double newBalance;

        if (type == "CASH_IN") {
            newBalance = oldBalance - amount;
            if (newBalance < 0.0) {
                newBalance = 0.0;
            }
        } else {
            newBalance = oldBalance + amount;
        }
        return newBalance;
    }

    public static double getRandomDouble(double min, double max) {
        Random r = new Random();
        double randomValue = min + (max - min) * r.nextDouble();
        randomValue = Math.round(randomValue * 100.0) / 100.0;
        return randomValue;
    }

    public static int getRandomInteger(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static String getRandomNameDest(String type) {
        String dest;

        if (type == "PAYMENT") {
            dest = "M";
        } else {
            dest = "C";
        }
        return dest;
    }
}
