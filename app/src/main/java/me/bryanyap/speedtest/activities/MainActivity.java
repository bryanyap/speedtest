package me.bryanyap.speedtest.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Calendar;

import me.bryanyap.speedtest.R;
import me.bryanyap.speedtest.services.SpeedTestService;
import me.bryanyap.speedtest.tasks.SpeedTestAsyncTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView speedText = null;
    private TextView statusText = null;
    private Button testButton = null;

    private Firebase fb = null;
    private SpeedTestAsyncTask speedTestAsyncTask = null;
    private SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (!testExists()) {
            int frequency = Integer.parseInt(prefs.getString("frequency", "10"));
            scheduleTest(frequency);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        speedText = (TextView) findViewById(R.id.text_speed);
        statusText = (TextView) findViewById(R.id.text_status);

        String email = prefs.getString("email", null);
        String password = prefs.getString("password", null);
        authenticate(email, password);

        speedTestAsyncTask = new SpeedTestAsyncTask(this);

        testButton = (Button) findViewById(R.id.button_test);
        if (testButton != null) {
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (speedTestAsyncTask.getStatus().equals(AsyncTask.Status.PENDING)) {
                        speedTestAsyncTask.execute();
                    } else if (speedTestAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)
                            || speedTestAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                        speedTestAsyncTask = new SpeedTestAsyncTask(MainActivity.this);
                        speedTestAsyncTask.execute();
                    }
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, AppPreference.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "Checking for settings change");
        if (data.getBooleanExtra("changed", false)) {
            Log.v(TAG, "Frequency changed, updating test schedule");
            int frequency = Integer.parseInt(prefs.getString("frequency", "10"));
            scheduleTest(frequency);
        }
    }

    public void setSpeedText(String input) {
        this.speedText.setText(input);
    }

    public void setStatusText(String input) {
        this.statusText.setText(input);
    }

    private void authenticate(String email, String password) {
        statusText.setText("Authenticating");

        if (email != null && password != null) {
            String firebaseURLString = "https://boiling-inferno-6791.firebaseio.com/";
            fb = new Firebase(firebaseURLString);
            fb.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    statusText.setText("Logged In");
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    statusText.setText("Authentication Failed");
                }
            });
        } else {
            statusText.setText("Please setup userid/password");
        }
    }

    private void scheduleTest(int frequency) {
        Calendar cal = Calendar.getInstance();

        Intent intent = new Intent(this, SpeedTestService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), frequency * 60 * 1000, pintent);

    }

    public boolean testExists() {
        Intent intent = new Intent(this, SpeedTestService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pintent == null) {
            return false;
        }
        return true;
    }

    public Firebase getFirebase() {
        return this.fb;
    }

}
