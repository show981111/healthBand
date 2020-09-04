package com.hanium.healthband.model;

public class SensorData {
    private String temperature;
    private String humidity;
    private String heartRate;
    private String steps;
    private String sound;

    public SensorData(String temperature, String humidity, String heartRate, String steps, String sound) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.heartRate = heartRate;
        this.steps = steps;
        this.sound = sound;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public String getSteps() {
        return steps;
    }

    public String getSound() {
        return sound;
    }
}
