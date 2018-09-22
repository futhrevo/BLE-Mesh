package in.hedera.reku.swimclock.scanner;

import android.bluetooth.le.ScanResult;

public interface ScannerInterface {

    void scanningStarted();
    void scanningStopped();
    void scanResult(ScanResult result);
}
