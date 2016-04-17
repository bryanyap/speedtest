package me.bryanyap.speedtest.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import me.bryanyap.speedtest.activities.SpeedTestUI;
import me.bryanyap.speedtest.constants.ApplicationConstants;
import me.bryanyap.speedtest.daos.TestResultDao;
import me.bryanyap.speedtest.daos.TestResultDaoImpl;
import me.bryanyap.speedtest.tasks.SpeedTestAsyncTask;

public class SpeedTestService extends Service implements ApplicationConstants, SpeedTestUI {
    private static final String TAG = "SpeedTestService";
    TestResultDao testResultDao = new TestResultDaoImpl();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SpeedTestAsyncTask speedTestAsyncTask = new SpeedTestAsyncTask(this, getBaseContext());
        Log.v(TAG, "Executing SpeedTestAsyncTask");
        speedTestAsyncTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        testResultDao.authenticate(email, password);
    }

    @Override
    public void setStatusText(String input) {

    }

    @Override
    public void setSpeedText(String input) {

    }

    @Override
    public void setTestButtonText(String input) {

    }

    @Override
    public void notifyDone() {

    }

    @Override
    public void notifyCancelled() {

    }
}