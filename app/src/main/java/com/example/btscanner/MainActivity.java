package com.example.btscanner;

import android.Manifest;
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

    private WifiManager wifiManager;
    private ArrayAdapter<String> listAdapter;
    private ListView wifiListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiListView = findViewById(R.id.wifiListView);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        wifiListView.setAdapter(listAdapter);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        Button scanWifiButton = findViewById(R.id.scanWifiButton);
        scanWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiPermissions();
            }
        });

        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item click if needed
            }
        });

        Button saveDataButton = findViewById(R.id.saveDataButton);
        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFile();
            }
        });
    }

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

        wifiManager.startScan();
        listAdapter.clear();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            String result = "SSID: " + scanResult.SSID
                    + "\nBSSID: " + scanResult.BSSID
                    + "\nSignal Strength: " + scanResult.level + "dBm"
                    + "\nFrequency: " + scanResult.frequency + "MHz";
            listAdapter.add(result);
        }
        listAdapter.notifyDataSetChanged();
    }


//    private void saveDataToFile() {
//        // Get the data from the ListView
//        int itemCount = listAdapter.getCount();
//        if (itemCount == 0) {
//            Toast.makeText(this, "No data available to save", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        StringBuilder dataBuilder = new StringBuilder();
//
//        // Read existing data from the file, if it exists
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
//                    dataBuilder.append(line).append("\n");
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
//        // Append new data with timestamp to the StringBuilder
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        String timestamp = dateFormat.format(new Date());
//
//        dataBuilder.append("[\n");
//
//        for (int i = 0; i < itemCount; i++) {
//            String item = listAdapter.getItem(i);
//            String[] lines = item.split("\\n");
//            StringBuilder ans = new StringBuilder();
//
//            // Extracting individual terms
//            for (String line : lines) {
//                String[] terms = line.split(":\\s");
//                if (terms.length == 2) {
//                    String key = terms[0].trim();
//                    String value = terms[1].trim();
//                    ans.append("'").append(key).append("'").append(":").append("'").append(value).append("',");
//                }
//            }
//
//
//            dataBuilder.append("[\n")
//                    .append("    'wifi'= {")
//                    .append(ans).append(" 'timestamp': '")
//                    .append(timestamp).append("'},\n")
//                    .append("    'bt'= {}\n")
//                    .append("]");
//            if (i < itemCount - 1) {
//                dataBuilder.append(",");
//            }
//            dataBuilder.append("\n");
//        }
//
//        dataBuilder.append("]\n");
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
//            FileWriter writer = new FileWriter(file, true); // Append to the file
//            writer.write(dataBuilder.toString());
//            writer.flush();
//            writer.close();
//
//            Toast.makeText(this, "Data saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void saveDataToFile() {
        // Get the data from the ListView
        int itemCount = listAdapter.getCount();
        if (itemCount == 0) {
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

        for (int i = 0; i < itemCount; i++) {
            String item = listAdapter.getItem(i);
            String[] lines = item.split("\\n");
            StringBuilder ans = new StringBuilder();

            // Extract individual terms
            for (String line : lines) {
                String[] terms = line.split(":\\s");
                if (terms.length == 2) {
                    String key = terms[0].trim();
                    String value = terms[1].trim();
                    ans.append("'").append(key).append("'").append(":").append("'").append(value).append("',");
                }
            }

            existingData.add("[");
            existingData.add("    'wifi' = {" + ans + " 'timestamp': '" + timestamp + "'},");
            existingData.add("    'bt' = {}");
            existingData.add("]");

            existingData.add(""); // Add an empty line between entries
        }

        // Save data to file
        try {
            String customDirectoryName = "my_custom_directory";
            String fileName = "data.txt";

            File customDirectory = new File(getExternalFilesDir(null), customDirectoryName);

            if (!customDirectory.exists()) {
                if (!customDirectory.mkdirs()) {
                    Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File file = new File(customDirectory, fileName);

            FileWriter writer = new FileWriter(file);
            for (String line : existingData) {
                writer.write(line + "\n");
            }
            writer.flush();
            writer.close();

            Toast.makeText(this, "Data saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }


}
