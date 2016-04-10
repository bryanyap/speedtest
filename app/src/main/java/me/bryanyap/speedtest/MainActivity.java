package me.bryanyap.speedtest;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Thread thread = new Thread(new Task());
    private Handler handler = new Handler();
    private TextView speedText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        speedText = (TextView) findViewById(R.id.text_speed);

        Button testButton = (Button) findViewById(R.id.button_test);
        if (testButton != null) {
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (thread.getState().equals(Thread.State.NEW)) {
                        thread.start();
                    } else if (thread.getState().equals(Thread.State.TERMINATED)) {
                        thread = new Thread(new Task());
                        thread.start();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Task implements Runnable {
        @Override
        public void run() {
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
                        final String speedString = String.valueOf(speed) + "KB/s";

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                speedText.setText(speedString);
                            }
                        });

                        Log.v(TAG, speedString);
                        counter = 0;
                    }
                }

                Long endTime = System.currentTimeMillis();
                Log.v(TAG, "endTime=" + endTime + "ms");
                double timeTaken = (endTime - startTime) / 1000.0;
                Log.v(TAG, "timeTaken=" + timeTaken + "s");
                Log.v(TAG, "size=" + size + "KB");
                input.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
