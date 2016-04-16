package me.bryanyap.speedtest.models;


import java.sql.Timestamp;

public class TestResult {
    private Timestamp timestamp;
    private String location;
    private String deviceID;
    private double peakSpeed;
    private double averageSpeed;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public double getPeakSpeed() {
        return peakSpeed;
    }

    public void setPeakSpeed(double peakSpeed) {
        this.peakSpeed = peakSpeed;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
}
