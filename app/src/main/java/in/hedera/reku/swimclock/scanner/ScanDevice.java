package in.hedera.reku.swimclock.scanner;

import android.bluetooth.BluetoothDevice;

public class ScanDevice {
    private BluetoothDevice device;
    private int rssi;

    public ScanDevice(BluetoothDevice device, int rssi){
        setDevice(device);
        setRssi(rssi);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getAddress(){
        return device.getAddress();
    }

    public String getName() {
        return device.getName();
    }
    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


}
