package in.hedera.reku.swimclock.scanner;

import android.bluetooth.BluetoothDevice;

public interface ScannerInterface {

    void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi);
    void scanningStarted();
    void scanningStopped();
}
