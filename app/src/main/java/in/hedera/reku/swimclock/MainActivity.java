package in.hedera.reku.swimclock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.silabs.bluetooth_mesh.BluetoothMesh;
import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.MeshCallback;
import com.silabs.bluetooth_mesh.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

import in.hedera.reku.swimclock.home.HomeFragment;
import in.hedera.reku.swimclock.scanner.BleScanner;
import in.hedera.reku.swimclock.scanner.ScannerFragment;
import in.hedera.reku.swimclock.scanner.ScannerInterface;
import in.hedera.reku.swimclock.settings.SettingsFragment;
import in.hedera.reku.swimclock.utils.Constants;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 101;
    private final Object mConnectionLock = new Object(); // Lock variable
    public boolean isBTEnabled;
    private TextView mTextMessage;
    private BluetoothMesh btmesh;
    private BluetoothLeService bleService;
    private boolean isBound = false;
    private String provisionMac;
    private String proxyMac;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    break;
                case R.id.navigation_scanner:
                    if (needPermissions(getApplicationContext())) {
                        requestPermissions();
                    }
                    if (getBTStatus() & isBTEnabled)
                        fragment = new ScannerFragment();
                    else {
                        askBTSettings();
                    }
                    break;
                case R.id.navigation_settings:
                    fragment = new SettingsFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // load the default fragment
        loadFragment(new HomeFragment());
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (needPermissions(this)) {
            requestPermissions();
        }

        // Create bluetooth mesh
        btmesh = new BluetoothMesh(getApplicationContext(), btmeshCallback);

        // start LE service
        Intent intent = new Intent(this, BluetoothLeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        registerReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBTStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ALL_PERMISSIONS:
                boolean hasAllPermissions = true;
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        hasAllPermissions = false;
                        Log.e(TAG, "Unable to get permission " + permissions[i]);
                    }
                }
                if (hasAllPermissions) {
                    Log.d(TAG, "All permissions granted");

                } else {
                    Toast.makeText(this,
                            "Unable to get all required permissions", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Permission has been denied by user");
//                    finish();
                    return;
                }

                break;
            default:
                Log.e(TAG, "Unexpected request code");

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    static public boolean needPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions: ");
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        };
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }

    public boolean getBTStatus() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            isBTEnabled = false;
            return false;
        } else {
            isBTEnabled = true;
            return true;
        }
    }

    public void askBTSettings() {
        if (!getBTStatus()) {
            showBluetoothEnableDialog();
        }
    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();
        isBTEnabled = true;
    }


    public void showBluetoothEnableDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable Bluetooth");
        // Setting Dialog Message
        alertDialog.setMessage("Bluetooth is required for all crucial functions. Do you want to turn ON bluetooth?");
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enableBT();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isBTEnabled = false;
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private final MeshCallback btmeshCallback = new MeshCallback() {
        @Override
        public void didSuccessProvision(int meshAddress, byte[] deviceUuid, int status) {
            super.didSuccessProvision(meshAddress, deviceUuid, status);
            if (status == 0) { // provison success
                addDeviceInfo(meshAddress, deviceUuid);
            }
        }

        @Override
        public void disconnectionRequest(int gattHandle) {
            super.disconnectionRequest(gattHandle);
            bleService.disconnect();
            btmesh.disconnectGatt(gattHandle);
        }

        @Override
        public void networkCreated(String name, int index, byte[] netKey) {
            super.networkCreated(name, index, netKey);
            NetworkInfo defaultNetwork = new NetworkInfo();
            defaultNetwork.setName(name);
            defaultNetwork.setNetwork_key(netKey);
            defaultNetwork.setNetID(index);
            btmesh.saveNetworkDB(defaultNetwork);
//            createDemoGroup(defaultNetwork);
        }

        @Override
        public void stateChanged(int state) {
            super.stateChanged(state);
            switch (state) {
                case BluetoothMesh.INITIALISED:
                    Log.d(TAG, "Mesh Initialized");
                    break;

                case BluetoothMesh.DEINITIALISED:
                    Log.d(TAG, "Mesh Deinitialized");
                    break;
            }
        }
    };

    private void addDeviceInfo(int meshAddress, byte[] deviceUuid) {
        DeviceInfo clock = new DeviceInfo("name", deviceUuid);
        NetworkInfo netinfo = btmesh.getNetworkDB().get(0);
        clock.addToNetwork(netinfo, meshAddress);
        netinfo.addDevice(clock);
        btmesh.saveNetworkDB(netinfo);

    }

    public void provisonDevice(String mac) {
        synchronized (mConnectionLock) {
            provisionMac = mac;
            if(!bleService.isConnected()) {
                bleService.connect(mac, BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            } else {
                bleService.disconnect();
            }
        }
    }

    public class ConnectProxy implements ScannerInterface {
        public final String TAG = ScannerFragment.class.getSimpleName();
        private static final long SCAN_TIMEOUT = 60000;

        private BleScanner bleScanner;

        public ConnectProxy(Context context) {
            bleScanner = new BleScanner(context);
            List<ScanFilter> filters = new ArrayList<>();
            ParcelUuid meshProxyServ = ParcelUuid.fromString(BluetoothMesh.meshProxyService.toString());
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(meshProxyServ).build();
            // TODO: Enable this line when testing with mesh devices
//          filters.add(filter);
            bleScanner.startScanning(this, SCAN_TIMEOUT, filters);
        }

        @Override
        public void scanningStarted() {

        }

        @Override
        public void scanningStopped() {

        }

        @Override
        public void scanResult(ScanResult result) {
            // First go through devices which don't have DCD entries yet, they might be
            // advertising with Identity instead of Network beacons, for example right
            // after provisioning

            boolean idMatch = false;
            boolean netMatch = false;
            ParcelUuid meshProxyServ = ParcelUuid.fromString(BluetoothMesh.meshProxyService.toString());
            NetworkInfo netInfo = btmesh.getNetworkDB().get(0);
            if (result.getScanRecord() != null && result.getScanRecord().getServiceUuids() != null && result.getScanRecord().getServiceUuids().contains(meshProxyServ)) {
                for (DeviceInfo devInfo : netInfo.devicesInfo()) {
                    if (devInfo.dcd() == null) {
                        if (btmesh.deviceIdentityMatches(result.getScanRecord().getBytes(), devInfo) >= 0) {
//                            identityMatchResults.add(result);
                            idMatch = true;
                            break;
                        }
                    }
                }
                // If we already have an Identity match, it won't match the Network beacon
                if (!idMatch && btmesh.networkHashMatches(netInfo, result.getScanRecord().getBytes()) >= 0) {
//                    networkMatchResults.add(result);
                    netMatch = true;
                }

                if (!idMatch && !netMatch) return;
                proxyMac = result.getDevice().getAddress();
                bleScanner.stopScanning();
                synchronized (mConnectionLock) {
                    if(!bleService.isConnected()) {
                        bleService.connect(proxyMac, BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
                    } else {
                        bleService.disconnect();
                    }
                }
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            bleService = binder.getService();
            isBound = true;
            if (!bleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            if (bleService != null) {
                bleService.disconnect();
                bleService.close();
            }

            bleService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_ERROR);
        intentFilter.addAction(Constants.ACTION_MTU_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return intentFilter;
    }

    private BroadcastReceiver gattUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {

            } else if (Constants.ACTION_GATT_SERVICES_ERROR.equals(action)) {

            } else if (Constants.ACTION_MTU_CHANGED.equals(action)) {

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

            }
        }
    };

    private void registerReceivers() {
        registerReceiver(gattUpdatesReceiver, makeGattUpdateIntentFilter());
    }


    private void unregisterReceivers() {
        unregisterReceiver(gattUpdatesReceiver);
    }

}
