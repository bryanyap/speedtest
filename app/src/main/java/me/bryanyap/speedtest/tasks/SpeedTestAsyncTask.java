package me.bryanyap.speedtest.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import me.bryanyap.speedtest.activities.SpeedTestUI;
import me.bryanyap.speedtest.constants.ApplicationConstants;
import me.bryanyap.speedtest.daos.TestResultDao;
import me.bryanyap.speedtest.daos.TestResultDaoImpl;
import me.bryanyap.speedtest.models.TestResult;
import me.bryanyap.speedtest.utils.Util;

public class SpeedTestAsyncTask extends AsyncTask<String, String, TestResult> implements ApplicationConstants {
    String TAG = "SpeedTestAsyncTask";
    private SpeedTestUI speedTestUI = null;
    private SharedPreferences prefs = null;
    private Util util = new Util();
    private TestResultDao testResultDao = new TestResultDaoImpl();

    public SpeedTestAsyncTask(SpeedTestUI speedTestUI, Context baseContext) {
        this.speedTestUI = speedTestUI;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(baseContext);
    }

    @Override
    protected TestResult doInBackground(String... params) {
        double peakSpeed = 0;
        double averageSpeed = 0;
        try {
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

        return util.generateResult(peakSpeed, averageSpeed, USER_INITIATED, prefs.getString(LOCATION, null));
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        speedTestUI.setStatusText(values[0]);
        speedTestUI.setSpeedText("Speed: " + values[1]);
    }

    @Override
    protected void onPostExecute(TestResult result) {
        super.onPostExecute(result);

        speedTestUI.setStatusText("Done");
        speedTestUI.setSpeedText("Average Speed: " + result.getAverageSpeed() + "KB/s");
        speedTestUI.notifyDone();

        testResultDao.write(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        speedTestUI.notifyCancelled();
    }
}
