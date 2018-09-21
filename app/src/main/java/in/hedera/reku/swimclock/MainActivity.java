package in.hedera.reku.swimclock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.silabs.bluetooth_mesh.MeshCallback;
import com.silabs.bluetooth_mesh.NetworkInfo;

import in.hedera.reku.swimclock.home.HomeFragment;
import in.hedera.reku.swimclock.scanner.ScannerFragment;
import in.hedera.reku.swimclock.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 101;
    private final Object mConnectionLock = new Object(); // Lock variable
    public boolean isBTEnabled;
    private TextView mTextMessage;
    private BluetoothMesh btmesh;
    private BluetoothGatt gatt;

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
        BluetoothMesh btmesh = new BluetoothMesh(getApplicationContext(), btmeshCallback);

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

    MeshCallback btmeshCallback = new MeshCallback() {
        @Override
        public void disconnectionRequest(int gattHandle) {
            super.disconnectionRequest(gattHandle);

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

    public void provisonDevice(String mac) {
        synchronized (mConnectionLock) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null || mac == null) {
                Log.e(TAG, "No Bluetooth Device or Bluetooth Adapter");
                return;
            }

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
            if (device == null) {
                Log.e(TAG, "Connect: device is null");
                return;
            }

            final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);

                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                }
            };

            BluetoothGatt gatt = device.connectGatt(this, false, gattCallback);
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            gatt.requestMtu(69);
        }
    }
}
