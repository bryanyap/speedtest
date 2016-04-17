package me.bryanyap.speedtest.utils;

import java.util.Date;

import me.bryanyap.speedtest.models.TestResult;

public class Util {
    private String TAG = "Util";
    public TestResult generateResult(double peakSpeed, double averageSpeed, String type, String location) {
        TestResult result = new TestResult();
        result.setPeakSpeed(peakSpeed);
        result.setAverageSpeed(averageSpeed);
        result.setType(type);
        result.setLocation(location);
        result.setTimestamp(new java.sql.Timestamp(new Date().getTime()));

        return result;
    }
}
