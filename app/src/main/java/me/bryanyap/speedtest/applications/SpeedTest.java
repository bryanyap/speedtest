package me.bryanyap.speedtest.applications;

import android.app.Application;

import com.firebase.client.Firebase;


public class SpeedTest extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

    }
}
