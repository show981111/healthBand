package com.hanium.healthband;

import java.util.HashMap;
/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String TEMPERATURE_MEASUREMENT = "00002a6e-0000-1000-8000-00805f9b34fb";
    public static String HUMIDITY_MEASUREMENT = "00002a6f-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String SOUND_MEASUREMENT = "84a28513-03b1-42fc-909f-a81147461d38";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String EXTRA_SERVICE = "d2ff1355-e7b4-4ba6-829f-a3af2a616ec4";
    public static String EXTRA_CHAR = "9c583724-c1dc-471a-8fc1-8558059dc2c5";

    public static String FALLING_CHAR = "7484ddab-daf3-45ff-afe5-8324ef2a51d1";
    public static String SWITCH_CHAR = "e0d9ba09-43c9-424a-8cca-2840189f7d3f";

    //public static String

    static {
        // Sample Services.

        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "HeartRate Measurement");
        attributes.put(SOUND_MEASUREMENT, "Sound Measurement");
        attributes.put(FALLING_CHAR, "Falling Measurement");
        attributes.put(SWITCH_CHAR, "Switch Measurement");

        attributes.put(TEMPERATURE_MEASUREMENT, "Temperature Measurement");
        attributes.put(HUMIDITY_MEASUREMENT, "Humidity Measurement");
        attributes.put(EXTRA_SERVICE, "EXTRA SERVICE");
        attributes.put(EXTRA_CHAR, "EXTRA Measurement");
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}