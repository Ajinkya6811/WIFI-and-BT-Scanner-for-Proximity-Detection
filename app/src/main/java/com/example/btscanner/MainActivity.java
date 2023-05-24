package com.example.btscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_ACCESS_WIFI_STATE = 1;
    private static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_PERMISSION_CHANGE_WIFI_STATE = 3;
    /////////////////////////////////////////////////////////////////////////
    private static final int REQUEST_PERMISSION_LOCATION = 5;

    String observationText="";

    private int observationNumber = 1;


    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.BLUETOOTH_ADMIN
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED};

    private ListView BluetoothlistView;
    private ArrayList<BluetoothDeviceInfo> mDeviceList = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    Button scanWifiButton;
    TextView observationNumberTextView;
    /////////////////////////////////////////////////////////////////////////

    private WifiManager wifiManager;
    private ArrayAdapter<String> listAdapter;
    private ListView wifiListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////////////////////////////////////////////////////////////////////////

        checkPermissions();
        observationNumberTextView = findViewById(R.id.observationNumberTextView);
        observationNumberTextView.setText("Observation Number: " + observationNumber);


        BluetoothlistView = (ListView) findViewById(R.id.bluetoothListView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /////////////////////////////////////////////////////////////////////////


        wifiListView = findViewById(R.id.wifiListView);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        wifiListView.setAdapter(listAdapter);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        scanWifiButton = findViewById(R.id.scanWifiButton);
        scanWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiPermissions();
                startBluetoothScan();

            }
        });

        /////////////////////////////////////////////////////////////////////////////////


        /////////////////////////////////////////////////////////////////////////////////



        Button saveDataButton = findViewById(R.id.saveDataButton);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFile();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////


    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            mDeviceList.clear();
            BluetoothlistView.setAdapter(null);

            mBluetoothAdapter.startDiscovery();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                // Check if the device already exists in the list
                BluetoothDeviceInfo existingDevice = findDeviceInList(device.getAddress());
                if (existingDevice != null) {
                    // Update the RSSI value
                    existingDevice.setRssi(rssi);
                } else {
                    // Add the device to the list
                    BluetoothDeviceInfo newDevice = new BluetoothDeviceInfo(device.getName(), device.getAddress(), rssi);
                    mDeviceList.add(newDevice);
                }

                BluetoothlistView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, mDeviceList));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                // Discovery started
                scanWifiButton.setEnabled(false);
                Toast.makeText(context, "Scanning for Bluetooth devices...", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery finished
                scanWifiButton.setEnabled(true);
                Toast.makeText(context, "Bluetooth scan finished", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private BluetoothDeviceInfo findDeviceInList(String address) {
        for (BluetoothDeviceInfo device : mDeviceList) {
            if (device.getAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////



    private void checkWifiPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE},
                    REQUEST_PERMISSION_ACCESS_WIFI_STATE);
        } else {
            startWifiScan();
        }
    }



    private void startWifiScan() {
        if (wifiManager == null) {
            Toast.makeText(this, "Wi-Fi is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show();
        }

        listAdapter.clear();
        wifiListView.setAdapter(null);
        wifiManager.startScan();

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            String result = "SSID: " + scanResult.SSID
                    + "\nMAC: " + scanResult.BSSID
                    + "\nSignal Strength: " + scanResult.level + "dBm"
                    + "\nFrequency: " + scanResult.frequency + "MHz";
            listAdapter.add(result);
        }
        wifiListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }



    private void saveDataToFile() {
        // Get the data from the ListView
        int itemCount = listAdapter.getCount();
        int bluetoothItemCount = mDeviceList.size();
        if (itemCount == 0 && bluetoothItemCount == 0) {
            Toast.makeText(this, "No data available to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read existing data from the file, if it exists
        List<String> existingData = new ArrayList<>();
        try {
            String customDirectoryName = "my_custom_directory";
            String fileName = "data.txt";

            File customDirectory = new File(getExternalFilesDir(null), customDirectoryName);
            File file = new File(customDirectory, fileName);

            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    existingData.add(line);
                }

                bufferedReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read existing data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append new data with timestamp to the existing data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());

        StringBuilder newData = new StringBuilder();
        newData.append("[");
        if (itemCount > 0) {
            // Wi-Fi data
            newData.append("\n'wifi' : [");
            for (int i = 0; i < itemCount; i++) {
                String item = listAdapter.getItem(i);
                String[] lines = item.split("\\n");

                newData.append("\n{");
                for (String line : lines) {
                    String[] terms = line.split(":\\s");
                    if (terms.length == 2) {
                        String key = terms[0].trim();
                        String value = terms[1].trim();
                        newData.append("\n'" + key + "' : '" + value + "',");
                    }
                }

                newData.append("\n},");
            }
            newData.append("\n],");
        }


        if (bluetoothItemCount > 0) {
            // Bluetooth data
            newData.append("\n'bt' : [");
            for (int i = 0; i < bluetoothItemCount; i++) {
                BluetoothDeviceInfo deviceInfo = mDeviceList.get(i);
                newData.append("\n{");
                newData.append("\n'name' : '" + deviceInfo.getName() + "',");
                newData.append("\n'address' : '" + deviceInfo.getAddress() + "',");
                newData.append("\n'rssi' : '" + deviceInfo.getRssi() + "',");
                newData.append("\n},");
            }
            newData.append("\n]");
        }
        newData.append("\n'timestamp' : '" + timestamp + "'");
        newData.append("\n'observation No :'"+observationNumber);

        newData.append("\n]");

        // Add the new data to the existing data
        existingData.add(newData.toString());
        observationNumber++;
        observationNumberTextView.setText("Observation Number: " + observationNumber);

        // Save the updated data to the file
        try {
            String customDirectoryName = "my_custom_directory";
            String fileName = "data.txt";

            File customDirectory = new File(getExternalFilesDir(null), customDirectoryName);
            customDirectory.mkdirs();
            File file = new File(customDirectory, fileName);

            FileWriter writer = new FileWriter(file);
            for (String line : existingData) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.flush();
            writer.close();

            Toast.makeText(this, "Data saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }



//    private void saveDataToFile() {
//        // Get the data from the ListView
//        int itemCount = listAdapter.getCount();
//        if (itemCount == 0) {
//            Toast.makeText(this, "No data available to save", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Read existing data from the file, if it exists
//        List<String> existingData = new ArrayList<>();
//        try {
//            String customDirectoryName = "my_custom_directory";
//            String fileName = "data.txt";
//
//            File customDirectory = new File(getExternalFilesDir(null), customDirectoryName);
//            File file = new File(customDirectory, fileName);
//
//            if (file.exists()) {
//                FileReader fileReader = new FileReader(file);
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    existingData.add(line);
//                }
//
//                bufferedReader.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to read existing data", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Append new data with timestamp to the existing data
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        String timestamp = dateFormat.format(new Date());
//
//        for (int i = 0; i < itemCount; i++) {
//            String item = listAdapter.getItem(i);
//            String[] lines = item.split("\\n");
//            StringBuilder ans = new StringBuilder();
//
//            // Extract individual terms
//            for (String line : lines) {
//                String[] terms = line.split(":\\s");
//                if (terms.length == 2) {
//                    String key = terms[0].trim();
//                    String value = terms[1].trim();
//                    ans.append("'").append(key).append("'").append(":").append("'").append(value).append("',");
//                }
//            }
//
//            existingData.add("[");
//            existingData.add("    'wifi' = {" + ans + " 'timestamp': '" + timestamp + "'},");
//            existingData.add("    'bt' = {}");
//            existingData.add("]");
//
//            existingData.add(""); // Add an empty line between entries
//        }
//
//        // Save data to file
//        try {
//            String customDirectoryName = "my_custom_directory";
//            String fileName = "data.txt";
//
//            File customDirectory = new File(getExternalFilesDir(null), customDirectoryName);
//
//            if (!customDirectory.exists()) {
//                if (!customDirectory.mkdirs()) {
//                    Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//
//            File file = new File(customDirectory, fileName);
//
//            FileWriter writer = new FileWriter(file);
//            for (String line : existingData) {
//                writer.write(line + "\n");
//            }
//            writer.flush();
//            writer.close();
//
//            Toast.makeText(this, "Data saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
//        }
//    }


}
