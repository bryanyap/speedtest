package me.bryanyap.speedtest.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import me.bryanyap.speedtest.models.TestResult;

public class SpeedTestService extends Service {
    private static final String TAG = "SpeedTestService";
    Firebase fb = null;
    private SharedPreferences prefs = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        double peakSpeed = 0;
        double averageSpeed = 0;
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            int testSize = Integer.parseInt(prefs.getString("testSize", "10"));

            URL url;
            if (testSize == 10) {
                url = new URL("http://www.speedtest.com.sg/test_random_10mb.zip");
            } else if (testSize == 100) {
                url = new URL("http://www.speedtest.com.sg/test_random_100mb.zip");
            } else {
                url = new URL("http://www.speedtest.com.sg/test_random_500mb.zip");
            }

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

        TestResult result =  generateResult(peakSpeed, averageSpeed);

        Firebase resultsRef = fb.child("results");
        Firebase newResultRef = resultsRef.push();

        newResultRef.setValue(result);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        authenticate(email, password);

    }

    private TestResult generateResult(double peakSpeed, double averageSpeed) {
        TestResult result = new TestResult();
        result.setPeakSpeed(peakSpeed);
        result.setAverageSpeed(averageSpeed);
        result.setTimestamp(new java.sql.Timestamp(new Date().getTime()));

        return result;
    }

    private void authenticate(String email, String password) {
        String firebaseURLString = "https://boiling-inferno-6791.firebaseio.com/";
        fb = new Firebase(firebaseURLString);
        fb.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

}