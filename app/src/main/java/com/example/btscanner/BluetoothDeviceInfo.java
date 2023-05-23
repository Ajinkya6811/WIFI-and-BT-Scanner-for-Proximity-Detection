package com.example.btscanner;

public class BluetoothDeviceInfo {

    private String name;
    private String address;
    private int rssi;

    public BluetoothDeviceInfo(String name, String address, int rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "SSID:"+name + "\nMAC:"+address + "\nSignal Strength:" + rssi + " dBm";
    }
}
