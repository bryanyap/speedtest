package me.bryanyap.speedtest.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.Calendar;

import me.bryanyap.speedtest.R;
import me.bryanyap.speedtest.constants.ApplicationConstants;
import me.bryanyap.speedtest.daos.TestResultDao;
import me.bryanyap.speedtest.daos.TestResultDaoImpl;
import me.bryanyap.speedtest.services.SpeedTestService;
import me.bryanyap.speedtest.tasks.SpeedTestAsyncTask;


public class MainActivity extends AppCompatActivity implements ApplicationConstants, SpeedTestUI {
    private static final String TAG = "MainActivity";

    private TextView speedText = null;
    private TextView statusText = null;
    private Button testButton = null;

    private TestResultDao testResultDao = new TestResultDaoImpl();
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

        speedTestAsyncTask = new SpeedTestAsyncTask(this ,this.getBaseContext());

        testButton = (Button) findViewById(R.id.button_test);
        if (testButton != null) {
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (testButton.getText().equals("Reset")) {
                        resetUI();
                    } else if (testButton.getText().equals("Test")) {
                        startTest();
                    } else if (testButton.getText().equals("Cancel")) {
                        cancelTest();
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
            startActivityForResult(i, SETTINGS_CHANGED_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_CHANGED_REQUEST && resultCode == AppPreference.RESULT_OK) {
            Log.v(TAG, "data.getBooleanExtra()=" + data.getBooleanExtra(CHANGED, false));
            if (data.getBooleanExtra("changed", false)) {
                Log.v(TAG, "Frequency changed, updating test schedule");
                int frequency = Integer.parseInt(prefs.getString(FREQUENCY, "10"));
                scheduleTest(frequency);
            }
        }

    }

    @Override
    public void setSpeedText(String input) {
        this.speedText.setText(input);
    }

    @Override
    public void setStatusText(String input) {
        this.statusText.setText(input);
    }

    @Override
    public void setTestButtonText(String input) {
        this.testButton.setText(input);
    }

    @Override
    public void notifyDone() {
        setTestButtonText("Reset");
    }

    @Override
    public void notifyCancelled() {
        resetUI();
    }

    private void cancelTest() {
        speedTestAsyncTask.cancel(true);
    }

    private void resetUI() {
        statusText.setText("Ready");
        speedText.setText("Speed: ");
        testButton.setText("Test");
    }

    private void startTest() {
        this.speedTestAsyncTask = new SpeedTestAsyncTask(this, this.getBaseContext());
        speedTestAsyncTask.execute();
        testButton.setText("Cancel");
    }

    private void authenticate(String email, String password) {
        statusText.setText("Authenticating");
        Log.v(TAG, "Authenticating");
        if (email != null && password != null) {
            testResultDao.authenticate(email, password);
            if (testResultDao.isAuthenticated()) {
                Log.v(TAG, "Logged in: " + email);
                statusText.setText("Logged In");
            } else {
                Log.v(TAG, "Authentication Failed");
                statusText.setText("Authentication Failed");
            }
        } else {
            Log.v(TAG, "Email or password not set up");
            statusText.setText("Please setup userid/password");
        }
    }

    private void scheduleTest(int frequency) {
        Calendar cal = Calendar.getInstance();

        Intent intent = new Intent(this, SpeedTestService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

//        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), frequency * 60 * 1000, pintent);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), frequency * 1000, pintent);

//        Log.v(TAG, "Scheduled test with frequency of " + frequency + "mins");
        Log.v(TAG, "Scheduled test with frequency of " + frequency + "seconds");
    }

    public boolean testExists() {
        Intent intent = new Intent(this, SpeedTestService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pintent == null) {
            return false;
        }
        return true;
    }



}
