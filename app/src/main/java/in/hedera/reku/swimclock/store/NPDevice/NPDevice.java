package in.hedera.reku.swimclock.store.NPDevice;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import in.hedera.reku.swimclock.scanner.ScanDevice;

@Entity(tableName = "scanned_devices")
public class NPDevice {

    @PrimaryKey
    @NonNull
    private String mac;
    private String name;
    private int rssi;

    public NPDevice(@NonNull String mac, String name, int rssi) {
        this.mac = mac;
        this.name = name;
        this.rssi = rssi;
    }

    public NPDevice(@NonNull ScanDevice device) {
        mac = device.getAddress();
        name = device.getName() != null ? device.getName() : "Unknown Device";
        rssi = device.getRssi();
    }

    public NPDevice(@NonNull BluetoothDevice device, int rssi) {
        mac = device.getAddress();
        name = device.getName() != null ? device.getName() : "Unknown Device";
        this.rssi = rssi;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
