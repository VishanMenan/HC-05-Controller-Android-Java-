package com.example.ardbluetoothcontrollerapp;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private final String DEVICE_ADDRESS = "98:DA:50:02:B6:FE"; // Replace with your HC-05 MAC address
    private final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID for HC-05

    private TextView btConnectionStatus;
    private TextView btConnectionStrength;

    private Vibrator vibrator;
    private boolean isVibrating = false;

    private ObjectAnimator rotationAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //Initialise Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bluetooth Boat Controller");
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hides the ActionBar title

        // Initialize the refresh button
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);

        //Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        //Initialize controller buttons
        ImageButton btnForward = findViewById(R.id.btnForward);
        ImageButton btnBackward = findViewById(R.id.btnBackward);
        ImageButton btnLeft = findViewById(R.id.btnLeft);
        ImageButton btnRight = findViewById(R.id.btnRight);

        Button btnStop = findViewById(R.id.btnStop);
        btConnectionStatus = findViewById(R.id.btConnectionStatus);
        btConnectionStrength = findViewById(R.id.btConnectionStrength);

        updateConnectionStatus(false);

        //Button Listeners
        /*btnForward.setOnClickListener(v -> sendCommand("F")); // Send 'F' for Forward
        btnBackward.setOnClickListener(v -> sendCommand("B")); // Send 'B' for Backward
        btnLeft.setOnClickListener(v -> sendCommand("L")); // Send 'L' for Left
        btnRight.setOnClickListener(v -> sendCommand("R")); // Send 'R' for Right*/

        //Latest button listeners
        btnForward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // Button pressed
                    sendCommand("F"); // Send 'F' for Forward
                    return true;
                case MotionEvent.ACTION_UP: // Button released
                    sendCommand("S"); // Send 'N' for Neutral/Stop
                    //v.performClick(); // Call performClick for accessibility
                    return true;
            }
            return false;
        });

        btnBackward.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("B");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });

        btnLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("L");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });

        btnRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand("R");
                    return true;
                case MotionEvent.ACTION_UP:
                    sendCommand("S");
                    return true;
            }
            return false;
        });

        btnStop.setOnClickListener(v -> sendCommand("S")); // Send 'S' for STOP

        // Set click listener for the refresh button
//        btnRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                rotateButton(btnRefresh);
//
//                // Update connection status and strength
//                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
//                    updateConnectionStatus(true);
//                } else {
//                    updateConnectionStatus(false);
//                }
//                updateConnectionStrength();
//                btnRefresh.animate().cancel();
//            }
//        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateButton(btnRefresh);

                // Simulate refreshing process
                new Handler().postDelayed(() -> {
                    // Update connection status and strength
                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                        updateConnectionStatus(true);
                    } else {
                        updateConnectionStatus(false);
                    }
                    updateConnectionStrength();

                    // Stop the rotation animation
                    stopButtonRotation();
                }, 2000); // Simulate a delay for the refresh process (e.g., 2 seconds)
            }
        });

    }

    private void rotateButton(ImageButton btnRefresh) {
        rotationAnimator = ObjectAnimator.ofFloat(btnRefresh, "rotation", 0f, 360f);
        rotationAnimator.setDuration(1000); // Set duration for the rotation (1 second)
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat until explicitly stopped
        rotationAnimator.start();
    }

    private void stopButtonRotation() {
        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.cancel();
            rotationAnimator = null;
        }
    }

    private void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            btConnectionStatus.setText("Connection Status: Connected");
            btConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            btConnectionStatus.setText("Connection Status: Not Connected");
            btConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

//    private void updateConnectionStrength() {
//        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
//            try {
//                // Get the RSSI (Signal strength) value
//                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
//                int rssi = bluetoothAdapter.getRemoteDevice(device.getAddress()).getBondState();
//
//                // Update the TextView with the signal strength
//                String strengthText = "Connection Strength: ";
//                if (rssi > -50) {
//                    strengthText += "Excellent";
//                } else if (rssi > -70) {
//                    strengthText += "Good";
//                } else if (rssi > -90) {
//                    strengthText += "Weak";
//                } else {
//                    strengthText += "Very Weak";
//                }
//
//                btConnectionStrength.setText(strengthText);
//            } catch (Exception e) {
//                btConnectionStrength.setText("Connection Strength: Unknown");
//            }
//        } else {
//            btConnectionStrength.setText("Connection Strength: Unknown");
//        }
//    }

    private void updateConnectionStrength() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                // Get the RSSI (Signal strength) value
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                int rssi = bluetoothAdapter.getRemoteDevice(device.getAddress()).getBondState();

                // Update the TextView with the signal strength
                String strengthText = "Connection Strength: ";
                if (rssi > -50) {
                    strengthText += "Excellent";
                } else if (rssi > -70) {
                    strengthText += "Good";
                } else if (rssi > -90) {
                    strengthText += "Weak";
                } else {
                    strengthText += "Very Weak";
                }

                btConnectionStrength.setText(strengthText);
            } catch (Exception e) {
                btConnectionStrength.setText("Connection Strength: Unknown");
            }
        } else {
            btConnectionStrength.setText("Connection Strength: Unknown");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectDevice();
    }

    private void connectToDevice() {
        // Check for Bluetooth Connect permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not already granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }
        try {
            // Attempt to connect to the HC-05 device
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();

            updateConnectionStatus(true);
            updateConnectionStrength();
            Toast.makeText(this, "Connected to HC-05", Toast.LENGTH_SHORT).show();

            // Start listening for real-time sensor data
            listenForSensorData();
        } catch (IOException e) {
            e.printStackTrace();
            updateConnectionStatus(false);
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with Bluetooth connection
                connectToDevice();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void disconnectDevice() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();

            updateConnectionStatus(false);
            btConnectionStrength.setText("Connection Strength: Unknown");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendCommand(String command) {
        try {
            if(outputStream != null) {
                outputStream.write(command.getBytes());
                Toast.makeText(this, "Sent: " + command, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForSensorData() {
        new Thread(() -> {
            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    InputStream inputStream = bluetoothSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String receivedData;

                    while ((receivedData = reader.readLine()) != null) {
                        parseAndDisplaySensorData(receivedData.trim());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


//    private void parseAndDisplaySensorData(String data) {
//        if (data.contains("LEFT:") && data.contains("RIGHT:")) {
//            String[] parts = data.split(";");
//            String leftData = parts[0].replace("LEFT:", "").trim();
//            String rightData = parts[1].replace("RIGHT:", "").trim();
//
//            int leftDistance = Integer.parseInt(leftData);
//            int rightDistance = Integer.parseInt(rightData);
//
//            // Update the TextViews on the UI thread
//            runOnUiThread(() -> {
//                TextView leftSensorTextView = findViewById(R.id.leftSensorDistance);
//                TextView rightSensorTextView = findViewById(R.id.rightSensorDistance);
//
//                leftSensorTextView.setText(leftDistance + " cm");
//                rightSensorTextView.setText(rightDistance + " cm");
//
//                // Change border color if either distance <50
//                if (leftDistance < 50) {
//                    leftSensorTextView.setBackgroundResource(R.drawable.border_red);
//                } else {
//                    leftSensorTextView.setBackgroundResource(R.drawable.border_black);
//                }
//
//                if (rightDistance < 50) {
//                    rightSensorTextView.setBackgroundResource(R.drawable.border_red);
//                } else {
//                    rightSensorTextView.setBackgroundResource(R.drawable.border_black);
//                }
//
//                // Vibrate if either distance is <50
//                if (leftDistance < 50 || rightDistance < 50) {
//                    triggerVibration(true);
//                } else {
//                    triggerVibration(false);
//                }
//            });
//        }
//    }

//    private void parseAndDisplaySensorData(String data) {
//        if (data.contains("LEFT:") && data.contains("RIGHT:")) {
//            try {
//                String[] parts = data.split(";");
//                String leftData = parts[0].replace("LEFT:", "").trim();
//                String rightData = parts[1].replace("RIGHT:", "").trim();
//
//                int leftDistance = Integer.parseInt(leftData);
//                int rightDistance = Integer.parseInt(rightData);
//
//                // Update the TextViews on the UI thread
//                runOnUiThread(() -> {
//                    TextView leftSensorTextView = findViewById(R.id.leftSensorDistance);
//                    TextView rightSensorTextView = findViewById(R.id.rightSensorDistance);
//
//                    leftSensorTextView.setText(leftDistance + " cm");
//                    rightSensorTextView.setText(rightDistance + " cm");
//
//                    // Change border color if either distance <50
//                    leftSensorTextView.setBackgroundResource(leftDistance < 50 ? R.drawable.border_red : R.drawable.border_black);
//                    rightSensorTextView.setBackgroundResource(rightDistance < 50 ? R.drawable.border_red : R.drawable.border_black);
//
//                    // Vibrate if either distance is <50
//                    triggerVibration(leftDistance < 50 || rightDistance < 50);
//                });
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//                Log.e("BluetoothData", "Error parsing distances: " + data);
//            }
//        } else {
//            Log.d("BluetoothData", "Incomplete or unrecognized data: " + data);
//        }
//    }

    private void parseAndDisplaySensorData(String data) {
        try {
            // Ensure the data contains both LEFT and RIGHT
            if (data.contains("RIGHT:") && data.contains("LEFT:")) {
                String[] parts = data.split(";");
                String leftData = parts[1].replace("LEFT:", "").trim();
                String rightData = parts[0].replace("RIGHT:", "").trim();

                // Parse the distances
                int leftDistance = Integer.parseInt(leftData);
                int rightDistance = Integer.parseInt(rightData);

                // Update the TextViews on the UI thread
                runOnUiThread(() -> {
                    TextView leftSensorTextView = findViewById(R.id.leftSensorDistance);
                    TextView rightSensorTextView = findViewById(R.id.rightSensorDistance);

                    leftSensorTextView.setText(leftDistance + " cm");
                    rightSensorTextView.setText(rightDistance + " cm");

                    // Update borders and vibration here if needed
                    if (leftDistance < 15) {
                        leftSensorTextView.setBackgroundResource(R.drawable.border_red);
                    } else {
                        leftSensorTextView.setBackgroundResource(R.drawable.border_black);
                    }

                    if (rightDistance < 15) {
                        rightSensorTextView.setBackgroundResource(R.drawable.border_red);
                    } else {
                        rightSensorTextView.setBackgroundResource(R.drawable.border_black);
                    }

                    // Vibrate if either distance is <50
                    if (leftDistance < 15 || rightDistance < 15) {
                        triggerVibration(true);
                    } else {
                        triggerVibration(false);
                    }
                });
            } else {
                Log.e("BluetoothApp", "Error parsing distances: " + data);
            }
        } catch (Exception e) {
            Log.e("BluetoothApp", "Error parsing distances: " + data, e);
        }
    }



    private void triggerVibration(boolean start) {
        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (start) {
            if (!isVibrating) {
                isVibrating = true;
                long[] vibrationPattern = {0, 500, 500};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0)); // 0 for repeating
                } else {
                    vibrator.vibrate(vibrationPattern, 0); // Deprecated, but works for older Android versions
                }
            }
        } else {
            if (isVibrating) {
                isVibrating = false;
                vibrator.cancel(); // Stop the vibration
            }
        }
    }

}