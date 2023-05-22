package com.example.btscanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

import java.util.List;

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
}
