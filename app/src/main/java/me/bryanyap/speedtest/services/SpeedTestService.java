package me.bryanyap.speedtest.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import me.bryanyap.speedtest.constants.ApplicationConstants;
import me.bryanyap.speedtest.daos.TestResultDao;
import me.bryanyap.speedtest.daos.TestResultDaoImpl;
import me.bryanyap.speedtest.models.TestResult;
import me.bryanyap.speedtest.utils.Util;

public class SpeedTestService extends Service implements ApplicationConstants {
    private static final String TAG = "SpeedTestService";
    TestResultDao testResultDao = new TestResultDaoImpl();
    private Util util = new Util();
    private SharedPreferences prefs = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        double peakSpeed = 0;
        double averageSpeed = 0;
        try {
            prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            int testSize = Integer.parseInt(prefs.getString("testSize", "10"));

            URL url;
            switch (testSize) {
                case 10:
                    url = new URL(URL_10MB);
                    break;
                case 100:
                    url = new URL(URL_100MB);
                    break;
                case 500:
                    url = new URL(URL_500MB);
                    break;
                default:
                    url = new URL(URL_10MB);
                    break;
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

        TestResult result = util.generateResult(peakSpeed, averageSpeed);
        testResultDao.write(result);

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
        testResultDao.authenticate(email, password);

    }

}