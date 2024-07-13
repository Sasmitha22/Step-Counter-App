package com.example.stepcounterapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;

    private SensorManager mSensorManager = null;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int previousTotalSteps = 0;
    private ProgressBar progressBar;
    private TextView steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);

        loadData();
        resetSteps();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        if (stepSensor == null) {
            Toast.makeText(this, "This device has no step counter sensor", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("MainActivity", "Step Counter Sensor is available.");
        }

        // Check for activity recognition permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensor != null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED) {
            mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("MainActivity", "Sensor listener registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepSensor != null) {
            mSensorManager.unregisterListener(this);
            Log.d("MainActivity", "Sensor listener unregistered.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - previousTotalSteps;
            steps.setText(String.valueOf(currentSteps));
            progressBar.setProgress(currentSteps);

            // Log the step data for debugging
            Log.d("MainActivity", "Total Steps: " + totalSteps);
            Log.d("MainActivity", "Current Steps: " + currentSteps);
        } else {
            Log.d("MainActivity", "Received sensor event for unknown sensor type: " + event.sensor.getType());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    private void resetSteps() {
        steps.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Long press to reset steps", Toast.LENGTH_SHORT).show());

        steps.setOnLongClickListener(v -> {
            previousTotalSteps = totalSteps;
            steps.setText("0");
            progressBar.setProgress(0);
            saveData();
            return true;
        });
    }

    private void saveData() {
        SharedPreferences sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("key1", previousTotalSteps);
        editor.apply();
        Log.d("MainActivity", "Data saved: previousTotalSteps = " + previousTotalSteps);
    }

    private void loadData() {
        SharedPreferences sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        previousTotalSteps = sharedPref.getInt("key1", 0);
        totalSteps = previousTotalSteps;
        Log.d("MainActivity", "Data loaded: previousTotalSteps = " + previousTotalSteps);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (stepSensor != null) {
                    mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d("MainActivity", "Sensor listener registered after permission granted.");
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot access step counter sensor.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
