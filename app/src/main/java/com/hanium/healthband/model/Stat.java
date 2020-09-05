package com.hanium.healthband.model;

public class Stat {
    private float mean;
    private float max;
    private float min;
    private String date;



    public Stat(float mean, float max, float min, String date) {
        this.mean = mean;
        this.max = max;
        this.min = min;
        this.date = date;
    }
    public String getDate() {
        return date;
    }
    public float getMean() {
        return mean;
    }

    public void setMean(int mean) {
        this.mean = mean;
    }

    public float getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
