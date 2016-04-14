package me.bryanyap.speedtest.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.client.Firebase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import me.bryanyap.speedtest.activities.MainActivity;
import models.TestResult;

public class SpeedTestAsyncTask extends AsyncTask<String, String, TestResult> {
    String TAG = "SpeedTestAsyncTask";
    private MainActivity mainActivity = null;

    private double averageSpeed = 0;

    public SpeedTestAsyncTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected TestResult doInBackground(String... params) {
        double peakSpeed = 0;
        try {
            URL url = new URL("http://www.speedtest.com.sg/test_random_10mb.zip");
            InputStream input = url.openConnection().getInputStream();

            byte[] data = new byte[1024];

            Long startTime = System.currentTimeMillis();
            Log.v(TAG, "startTime=" + startTime + "ms");
            int counter = 0;
            int size = 0;
            Long startSpeedTimer = System.currentTimeMillis();
            while (true) {
                if (counter == 0) {
                    startSpeedTimer = System.currentTimeMillis();
                }
                if (input.read(data) != -1) {
                    counter++;
                    size++;
                } else {
                    break;
                }
                if (counter == 1000) {
                    Long endSpeedTimer = System.currentTimeMillis();
                    double timeDifference = (endSpeedTimer - startSpeedTimer) / 1000.0;
                    double speed = Math.round(1000.0 / timeDifference * 100) / 100;

                    if (speed > peakSpeed) {
                        peakSpeed = speed;
                    }

                    publishProgress("Running", String.valueOf(speed) + "KB/s", "");

                    Log.v(TAG, String.valueOf(speed) + "KB/s");
                    counter = 0;
                }
            }

            Long endTime = System.currentTimeMillis();
            Log.v(TAG, "endTime=" + endTime + "ms");
            double timeTaken = (endTime - startTime) / 1000.0;
            Log.v(TAG, "timeTaken=" + timeTaken + "s");
            Log.v(TAG, "size=" + size + "KB");

            averageSpeed = Math.round(size / timeTaken * 100) / 100;
            Log.v(TAG, "averageSpeed=" + averageSpeed + "KB/s");

            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return generateResult(peakSpeed, averageSpeed);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        mainActivity.setStatusText("Status: " + values[0]);
        mainActivity.setSpeedText("Speed: " + values[1]);
    }

    @Override
    protected void onPostExecute(TestResult result) {
        super.onPostExecute(result);
        mainActivity.setStatusText("Done");
        mainActivity.setSpeedText("Average Speed: " + result.getAverageSpeed() + "KB/s");

        Firebase resultsRef = mainActivity.getFirebase().child("results");
        Firebase newResultRef = resultsRef.push();

        newResultRef.setValue(result);
    }

    private TestResult generateResult(double peakSpeed, double averageSpeed) {
        TestResult result = new TestResult();
        result.setPeakSpeed(peakSpeed);
        result.setAverageSpeed(averageSpeed);
        result.setTimestamp(new java.sql.Timestamp(new Date().getTime()));

        return result;
    }
}
