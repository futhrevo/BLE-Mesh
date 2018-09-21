package in.hedera.reku.swimclock.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.silabs.bluetooth_mesh.BluetoothMesh;

import java.util.ArrayList;
import java.util.List;


public class BleScanner {
    public static final String TAG = BleScanner.class.getSimpleName();
    private BluetoothLeScanner scanner = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler = new Handler();
    private ScannerInterface scannerInterface;
    private Context context;
    private boolean scanning = false;
    private String device_name_start = "";

    public BleScanner(Context context){
        this.context = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // check bluetooth is available and on
        if(bluetoothAdapter == null) {
            Log.e(TAG, "No Bluetooth adapter available");
            // TODO: Cancel and go to home screen
        }

        if(!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    public void startScanning(final ScannerInterface scannerInterface, long duration) {
        if(scanning) {
            Log.d(TAG, "Already in scanning mode");
            return;
        }

        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            Log.d(TAG, "Bluetooth Scanner created");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(scanning) {
                    Log.d(TAG, "Scan Timeout: Stopping scanner");
                    scanner.stopScan(scan_callback);
                    setScanning(false);
                }
            }
        }, duration);

        this.scannerInterface = scannerInterface;
        Log.d(TAG, "Scanning");
        List<ScanFilter> filters = new ArrayList<>();
        ParcelUuid meshServ = ParcelUuid.fromString(BluetoothMesh.meshUnprovisionedService.toString());
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(meshServ).build();
        // TODO: Enable this line when testing with mesh devices
//        filters.add(filter);
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        setScanning(true);
        scanner.startScan(filters, settings, scan_callback);
    }

    public void stopScanning(){
        setScanning(false);
        Log.d(TAG, "Stopping scan");
        scanner.stopScan(scan_callback);
    }

    private ScanCallback scan_callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(!scanning){
                return;
            }
            scannerInterface.candidateBleDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
        }
    };

    public boolean isScanning(){
        return scanning;
    }

    void setScanning(boolean scanning){
        this.scanning = scanning;
        if(!scanning) {
            scannerInterface.scanningStopped();
        } else {
            scannerInterface.scanningStarted();
        }
    }
}
