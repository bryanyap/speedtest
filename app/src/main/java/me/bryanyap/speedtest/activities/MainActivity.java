package me.bryanyap.speedtest.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import me.bryanyap.speedtest.R;
import me.bryanyap.speedtest.tasks.SpeedTestAsyncTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView speedText = null;
    private TextView statusText = null;
    private Button testButton = null;

    private Firebase firebase = null;

    private SpeedTestAsyncTask speedTestAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    protected void onStart() {
        super.onStart();
        speedTestAsyncTask = new SpeedTestAsyncTask(this);

        speedText = (TextView) findViewById(R.id.text_speed);
        statusText = (TextView) findViewById(R.id.text_status);

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

        String firebaseURLString = "https://boiling-inferno-6791.firebaseio.com/";

        firebase = new Firebase(firebaseURLString);
        firebase.authWithPassword("bryanyap2004@gmail.com", "P@ssw0rd", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                statusText.setText("Logged In");
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                statusText.setText("Auth Error");
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSpeedText(String input) {
        this.speedText.setText(input);
    }

    public void setStatusText(String input) {
        this.statusText.setText(input);
    }

    public Firebase getFirebase() {
        return this.firebase;
    }

}
