package me.bryanyap.speedtest.utils;

import java.util.Date;

import me.bryanyap.speedtest.models.TestResult;

public class Util {
    public TestResult generateResult(double peakSpeed, double averageSpeed) {
        TestResult result = new TestResult();
        result.setPeakSpeed(peakSpeed);
        result.setAverageSpeed(averageSpeed);
        result.setTimestamp(new java.sql.Timestamp(new Date().getTime()));

        return result;
    }
}
