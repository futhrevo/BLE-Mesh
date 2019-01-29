package in.hedera.reku.swimclock;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.silabs.bluetooth_mesh.BluetoothMesh;
import com.silabs.bluetooth_mesh.ConfigOperation;
import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;
import com.silabs.bluetooth_mesh.MeshCallback;
import com.silabs.bluetooth_mesh.Model;
import com.silabs.bluetooth_mesh.NetworkInfo;
import com.silabs.bluetooth_mesh.Utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import de.halfbit.tinymachine.StateHandler;
import de.halfbit.tinymachine.TinyMachine;
import in.hedera.reku.swimclock.clock.ClockFragment;
import in.hedera.reku.swimclock.home.HomeFragment;
import in.hedera.reku.swimclock.scanner.BleScanner;
import in.hedera.reku.swimclock.scanner.NPDViewModel;
import in.hedera.reku.swimclock.scanner.ScannerFragment;
import in.hedera.reku.swimclock.scanner.ScannerInterface;
import in.hedera.reku.swimclock.settings.SettingsFragment;
import in.hedera.reku.swimclock.utils.Constants;
import in.hedera.reku.swimclock.utils.ProgressDialog;

import static com.silabs.bluetooth_mesh.ConfigOperation.Status.mesh_foundation_status_success;
import static in.hedera.reku.swimclock.utils.Constants.MESH_GROUP_APPKEY;
import static in.hedera.reku.swimclock.utils.Constants.MESH_INIT;
import static in.hedera.reku.swimclock.utils.Constants.MESH_READY;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_CONNECT;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_DISCONNECT;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_INIT;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_SEND;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_START;
import static in.hedera.reku.swimclock.utils.Constants.PROVISION_WRITE;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_DCD;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_DCD_WRITE_SEND;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_INIT;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_READY;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_SEND;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_SET;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_SET_SEND;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_SET_WRITE;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_START;
import static in.hedera.reku.swimclock.utils.Constants.PROXY_WRITE;
import static in.hedera.reku.swimclock.utils.Constants.REQ_CONNECT_GATT_PROV;
import static in.hedera.reku.swimclock.utils.Constants.REQ_CONNECT_GATT_PROXY;
import static in.hedera.reku.swimclock.utils.Constants.REQ_CONNECT_HIGH_RSSI;
import static in.hedera.reku.swimclock.utils.Constants.REQ_CONNECT_PROXY;
import static in.hedera.reku.swimclock.utils.Constants.REQ_DCD;
import static in.hedera.reku.swimclock.utils.Constants.REQ_HIDE_DIALOG;

public class MainActivity extends AppCompatActivity implements FragListener, Handler.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 101;
    private final Handler handler = new Handler(this);
    public boolean isBTEnabled;
    private BluetoothMesh btmesh;
    private BluetoothLeService bleService;
    private boolean isBound = false;
    private boolean isGattPending = false;
    private boolean isScanPending = false;
    private int gattPriority = BluetoothGatt.CONNECTION_PRIORITY_BALANCED;
    private NetworkInfo netInfo;
    private GroupInfo groupInfo;
    private DeviceInfo deviceInfo;

    private TinyMachine meshMachine;
    private TinyMachine provisionMachine;
    private TinyMachine proxyMachine;
    private Snackbar snackbar;
    private UnProvDevice unProvDevice;
    private ProxyDevice proxyDevice;
    private ProgressDialog progressDialog;
    private BottomNavigationView navigation;
    ArrayList<ScanResult> identityMatchResults;
    ArrayList<ScanResult> networkMatchResults;
    private Model onoffmodel = new Model(false, -1412627713, "Vendor");
    private int elementId = 0;
    private boolean status = false;
    private boolean isScanning = false;
    private boolean proxyPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        snackbar = Snackbar.make(findViewById(R.id.container), "Test Snack", Snackbar.LENGTH_SHORT);
        // load the default fragment
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new HomeFragment(), Constants.HOME_TAG);
        if (needPermissions(this)) {
            requestPermissions();
        }

        meshMachine = new TinyMachine(new MeshHandler(), MESH_INIT);
        provisionMachine = new TinyMachine(new ProvisionHandler(), Constants.PROVISION_INIT);
        proxyMachine = new TinyMachine(new ProxyHandler(), Constants.PROXY_INIT);

        meshMachine.fireEvent("INIT");
        // start LE service
        Intent intent = new Intent(this, BluetoothLeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        registerReceivers();
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBTStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    private final MeshCallback btmeshCallback = new MeshCallback() {
        @Override
        public void appKeyCreated(String name, int netKeyIndex, int appKeyIndex, byte[] appKey) {
            super.appKeyCreated(name, netKeyIndex, appKeyIndex, appKey);
            Log.d(TAG, "BTMESH appKeyCreated ");
            meshMachine.transitionTo(MESH_READY);
            netInfo = btmesh.getNetworkbyID(netKeyIndex);
            int pub_address = 0xc000 + appKeyIndex * 2;
            int sub_address = 0xc000 + appKeyIndex * 2 + 1;
            groupInfo = new GroupInfo(name, netInfo, appKeyIndex, appKey, pub_address, sub_address);
            netInfo.addGroup(groupInfo);
            btmesh.bindLocalModels(netInfo, groupInfo);
            btmesh.saveNetworkDB(netInfo);
        }

        @Override
        public void gattWrite(int gattHandle, byte[] send) {
            super.gattWrite(gattHandle, send);
            Log.d(TAG, "BTMESH asking gattWrite gattHandle: " + gattHandle + " data : " + Converters.getHexValue(send)
                    + " length = " + send.length);
            if (provisionMachine.getCurrentState() >= PROVISION_START) {
                bleService.writeProvisionInChar(gattHandle, send);
                provisionMachine.transitionTo(PROVISION_SEND);
            } else if (proxyMachine.getCurrentState() >= PROXY_START) {
                bleService.writeProxyInChar(Constants.HANDLE_PROXY, send);
                proxyMachine.transitionTo(PROXY_DCD_WRITE_SEND);
            } else {
                bleService.writeProxyInChar(Constants.HANDLE_PROXY, send);
            }
        }

        @Override
        public void didReceiveDCD(byte[] dcd, int status) {
            super.didReceiveDCD(dcd, status);
            Log.d(TAG, "BTMESH asking didReceiveDCD" + String.valueOf(status));
            proxyPending = false;
            if (status != 0) {
                showDialog("Failed to fetch DCD", 2000);
                Log.e(TAG, "Fetching DCD failed");
                return;
            }
            proxyMachine.transitionTo(PROXY_READY);
            if (deviceInfo != null) {
                deviceInfo.setDcd(dcd);
                showDialog("DCD Received", 2000);
                netInfo.updateDeviceInfo(deviceInfo);
                btmesh.saveNetworkDB(netInfo);
                refreshHomeContent(netInfo);
                setProxy(deviceInfo);
                showProxySuccessDialog();
            } else {
                Log.e(TAG, "device info is null");
            }
            Log.d(TAG, " Got DCD : " + Converters.getHexValue(dcd));

        }

        @Override
        public void gattRequest(int gattHandle) {
            super.gattRequest(gattHandle);
            Log.d(TAG, "BTMESH asking gattRequest handle: " + String.valueOf(gattHandle));
            // TODO: delete this line
            // if(testProxygattReq(gattHandle)) return;

            if (bleService.isConnected()) {
                if (gattHandle == bleService.gattHandle) {
                    Log.d(TAG, "APP replied with connectGatt with handle: " + gattHandle);
                    btmesh.connectGatt(gattHandle);
                }
            }
            if (deviceInfo != null) {
                      Log.d(TAG, "APP replied with DCDRequest with handle: " + gattHandle + " in 0.5 seconds");
                    handler.sendEmptyMessageDelayed(REQ_DCD, 500);

            }
        }

        @Override
        public void didExportRequest(Intent shareIntent) {
            super.didExportRequest(shareIntent);
            Log.d(TAG, "BTMESH asking didExportRequest");
        }

        @Override
        public void didOOBAuthdisplay(byte[] uuid, int input_action, int input_size, byte[] auth_data) {
            super.didOOBAuthdisplay(uuid, input_action, input_size, auth_data);
            Log.d(TAG, "BTMESH asking didOOBAuthdisplay");
        }

        @Override
        public void didOOBAuthRequest(byte[] uuid, int output_action, int output_size) {
            super.didOOBAuthRequest(uuid, output_action, output_size);
            Log.d(TAG, "BTMESH asking didOOBAuthRequest");
        }

        @Override
        public void didCompleteConfig(ConfigOperation previous_cfg, ConfigOperation next_cfg) {
            super.didCompleteConfig(previous_cfg, next_cfg);
            Log.d(TAG, "BTMESH asking didCompleteConfig");
//            deviceInfo = previous_cfg.device();
//            groupInfo = previous_cfg.group();
//            netInfo = previous_cfg.network();
            int kind = previous_cfg.kind();
            int status = previous_cfg.status();
            if (kind == ConfigOperation.CFG_ENABLE_PROXY) {
                if (previous_cfg.convertStatus(status) == mesh_foundation_status_success) {
                    Log.i(TAG, "Proxy added succesfully");
                    proxyMachine.transitionTo(PROXY_READY);
                } else {
                    Log.e(TAG, "Proxy addition failed");
                    proxyMachine.transitionTo(PROXY_READY);
                }

            }
            switch (previous_cfg.convertStatus(status)) {
                case mesh_foundation_status_success:
                    Log.i(TAG, "mesh_foundation_status_success ");
                    showDialog("Success", 500);
                    break;
                case mesh_foundation_status_invalid_publish_params:
                    Log.e(TAG, "mesh_foundation_status_invalid_publish_params");
                    showDialog("Invalid publish params", 500);
                    break;
                case mesh_foundation_status_timeout:
                    Log.e(TAG, "mesh_foundation_status_timeout");
                    showDialog("Timeout", 500);
                    break;
                default:
                    showDialog("Unknown config Result ", 1000);
                    break;
            }
            btmesh.applyNextCfg();

            // btmesh.cancelPendingCfgs();
        }

        @Override
        public void statusCallback(int model, int device_address, int current_status, int target_status,
                                   int remaining_ms) {
            super.statusCallback(model, device_address, current_status, target_status, remaining_ms);
            Log.d(TAG, "BTMESH asking statusCallback");
        }

        @Override
        public void didSuccessProvision(int meshAddress, byte[] deviceUuid, int status) {
            super.didSuccessProvision(meshAddress, deviceUuid, status);
            Log.d(TAG, "BTMESH asking didSuccessProvision" + String.valueOf(status));
            if (status == 0) { // provison success
                showProvisionSuccessDialog(meshAddress, deviceUuid);

            } else {
                showDialog("Provisioning Failed", 500);
            }
            provisionMachine.transitionTo(PROVISION_INIT);
        }

        @Override
        public void disconnectionRequest(int gattHandle) {
            super.disconnectionRequest(gattHandle);
            Log.d(TAG, "BTMESH asking disconnectionRequest");
            bleService.disconnect();
//            bleService.close();
            btmesh.disconnectGatt(gattHandle);
            if (provisionMachine.getCurrentState() > PROVISION_INIT) {
                provisionMachine.transitionTo(PROVISION_DISCONNECT);
            }
        }

        @Override
        public void networkCreated(String name, int index, byte[] netKey) {
            // super.networkCreated(name, index, netKey);
            Log.d(TAG, "BTMESH asking networkCreated " + name + String.valueOf(index));
            netInfo = new NetworkInfo();
            netInfo.setName(name);
            netInfo.setNetwork_key(netKey);
            netInfo.setNetID(index);
            btmesh.saveNetworkDB(netInfo);
            // createDemoGroup(defaultNetwork);
            refreshHomeContent(netInfo);
            meshMachine.transitionTo(MESH_READY);
            createGroup("Default");
        }

        @Override
        public void stateChanged(int state) {
            // super.stateChanged(state);
            Log.d(TAG, "BTMESH asking stateChanged " + String.valueOf(state));
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

    private void refreshHomeContent(NetworkInfo netInfo) {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(Constants.HOME_TAG);
        if (homeFragment != null) {
            Log.i(TAG, "refreshing home content");
            homeFragment.onNetworkCreated(netInfo);
        }
    }

    @Override
    public void createNetwork(String name) {
        if (meshMachine.getCurrentState() == MESH_INIT) {
            byte[] netKey = BluetoothMesh.randomGenerator(16);
            Log.d(TAG, netKey.toString());
            btmesh.createNetwork(name, netKey);
        } else {
            Log.e(TAG, "Mesh is already initialized");
        }
    }

    @Override
    public void createGroup(String name) {
        if (meshMachine.getCurrentState() == MESH_READY) {
            byte[] appKey = BluetoothMesh.randomGenerator(16);
            meshMachine.transitionTo(MESH_GROUP_APPKEY);
            btmesh.createAppkey(name, netInfo, appKey);
        } else {
            Log.e(TAG, "Mesh is not ready to create group");
        }
    }

    @Override
    public void readNetInfo(Fragment frag) {
        if (btmesh != null) {
            // NetworkInfo netinfo = btmesh.getNetworkbyID(0);
            if (frag instanceof HomeFragment) {
                ((HomeFragment) frag).showNetInfo(netInfo);
            }
        }
    }

    @Override
    public void gotoScannerScreen() {
        if (needPermissions(getApplicationContext())) {
            requestPermissions();
        }
        if (getBTStatus() & isBTEnabled) {
            navigation.findViewById(R.id.navigation_scanner).performClick();
        } else {
            askBTSettings();
        }
    }

    @Override
    public void gotoHomeScreen() {
        navigation.findViewById(R.id.navigation_home).performClick();
    }

    @Override
    public void setProxy(DeviceInfo deviceInfo) {
        if (deviceInfo.dcd().proxySupport()) {
            proxyMachine.transitionTo(PROXY_SET);
            btmesh.setProxy(deviceInfo, netInfo, true);
        }
    }

    @Override
    public void setRelay(DeviceInfo deviceInfo) {

    }

    @Override
    public void connectNetwork() {
        if (!bleService.isConnected()) {
            if(!isScanning) {
                showDialog("Identifying Proxy..", 10000);
                handler.sendEmptyMessageDelayed(REQ_CONNECT_PROXY, 300);
            } else {
                Log.i(TAG, "Proxy scan already in progress");
            }
        } else {
            Log.e(TAG, "BLE is already connected with gattHandle : " + String.valueOf(bleService.gattHandle));
        }
    }

    @Override
    public void startProvision(String mac, String advertisement) {
        unProvDevice = new UnProvDevice(mac, advertisement);
        // if(testProxyinit()) return;

        if (meshMachine.getCurrentState() == MESH_READY && provisionMachine.getCurrentState() == PROVISION_INIT) {
            showDialog("Starting Provision", 5000);
            provisionMachine.transitionTo(PROVISION_CONNECT);
            connectGatt(Constants.HANDLE_PROVISION);
        } else {
            Log.e(TAG, "Mesh not ready for provisioning");
        }
    }

    @Override
    public void deleteNetwork() {
        if (btmesh != null) {
            btmesh.cleanDB();
        }
    }

    @Override
    public void disableUIinteraction(boolean on) {
        if (on) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public void addRemoveGroup(DeviceInfo deviceInfo, GroupInfo grpInfo) {
        if(grpInfo == null) {
            grpInfo = groupInfo;
        }
        if (grpInfo.devices().contains(deviceInfo)) {
            Log.i(TAG, "Removing device from demo group");
            btmesh.removeFromGroup(deviceInfo, 0, netInfo, grpInfo, onoffmodel);
        } else {
            Log.i(TAG, "Adding device to demo group with on off model");
            showDialog("Adding device to group", 2000);
            btmesh.addToGroup(deviceInfo, 0, netInfo, grpInfo, onoffmodel);
        }

    }

    @Override
    public void onOffSet(DeviceInfo deviceInfo, GroupInfo grpInfo) {
        status = !status;
        if(grpInfo == null) {
            grpInfo = groupInfo;
        }
//        if(deviceInfo == null) {
//            Log.d(TAG, "setting group message on");
////            btmesh.onOffSet(null, grpInfo, elementId, status, 100, 100, false, true);
////            return;
//        }
//        if (!grpInfo.devices().contains(deviceInfo)) {
//            Log.d(TAG, "device is not part of the group");
//            return;
//        }
        Log.d(TAG, "setting device on");
//        btmesh.onOffSet(deviceInfo, grpInfo, elementId, status, 100, 100, false, true);
        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Send Message to network").setMessage("Write here in hex")
                .setView(taskEditText).setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        sendOpcode(task);
                    }
                }).setNegativeButton("Cancel", null).create();
        dialog.show();
    }

    @Override
    public void factoryReset(DeviceInfo deviceInfo) {
        if(deviceInfo != null && netInfo != null) {
            btmesh.factoryResetDevice(deviceInfo, netInfo);
            sendOpcode("000C" + String.valueOf(deviceInfo.meshAddress()));
            showDialog("Factory Reset", 10000);
            return;
        }
    }

    @Override
    public void sendOpcode(String opcode) {
        if(bleService != null && bleService.isConnected() && bleService.gattHandle == Constants.HANDLE_PROXY) {
            Log.d(TAG, "opcode: " + opcode);
            long tz = TimeZone.getDefault().getOffset(System.currentTimeMillis());
            String time = Long.toHexString(System.currentTimeMillis() + tz);
            Log.d(TAG, "local time : " + time);
            byte[] bytes = Converters.hexToByteArray(time + opcode);
            Log.d(TAG, "bytes : " + Converters.getHexValue(bytes));
            bleService.writeClockInChar(bytes);
        } else {
            Log.i(TAG, "unable to send opcode: " + opcode);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case REQ_DCD:
                requestDCD();
                break;

            case REQ_CONNECT_PROXY:
                if(!isScanning) {
                    new ConnectProxy(getApplicationContext());
                } else {
                    Log.i(TAG, "Proxy scan already in progress");
                }

                break;

            case REQ_HIDE_DIALOG:
                progressDialog.dismiss();
                break;

            case REQ_CONNECT_HIGH_RSSI:
                selectHighestRssi();
                break;

            case REQ_CONNECT_GATT_PROXY:
                connectGatt(Constants.HANDLE_PROXY);
                break;

            case REQ_CONNECT_GATT_PROV:
                connectGatt(Constants.HANDLE_PROVISION);
                break;
        }

        return true;
    }

    private void requestDCD() {
        if(deviceInfo.dcd() == null) {
            btmesh.DCDRequest(deviceInfo);
            proxyMachine.transitionTo(PROXY_DCD);
        }else {
            proxyMachine.transitionTo(PROXY_READY);
        }
        // deviceInfo = null;
    }

    private void addDeviceInfo(String name, int meshAddress, byte[] deviceUuid) {
        deviceInfo = new DeviceInfo(name, deviceUuid);
        // NetworkInfo netinfo = btmesh.getNetworkbyID(0);
        deviceInfo.addToNetwork(netInfo, meshAddress);
        netInfo.addDevice(deviceInfo);
        btmesh.saveNetworkDB(netInfo);
    }

    private void showDialog(String message, long delay) {
        handler.removeMessages(REQ_HIDE_DIALOG);
        progressDialog.setMessage(message);
        progressDialog.show();
        handler.sendEmptyMessageDelayed(REQ_HIDE_DIALOG, delay);
    }

    public void connectGatt(int gattHandle) {
        if (bleService.isConnected()) {
            bleService.disconnect();
//            bleService.close();
            isGattPending = true;
            return;
        }
        showDialog("Connecting GATT...", 35000);
        isGattPending = false;
        if (gattHandle == Constants.HANDLE_PROVISION && unProvDevice != null) {
            gattPriority = BluetoothGatt.CONNECTION_PRIORITY_HIGH;
            bleService.connect(unProvDevice.getmac(), gattPriority, gattHandle);
            return;
        }
        if (gattHandle == Constants.HANDLE_PROXY && proxyDevice != null) {
            gattPriority = BluetoothGatt.CONNECTION_PRIORITY_BALANCED;
            bleService.connect(proxyDevice.getMac(), gattPriority, gattHandle);
            return;
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
        intentFilter.addAction(Constants.ACTION_CHARACTERISTIC_WRITE);
        intentFilter.addAction(Constants.ACTION_CHARACTERISTIC_CHANGE);
        return intentFilter;
    }

    private BroadcastReceiver gattUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int handle = intent.getIntExtra(Constants.ACTION_GATT_HANDLE, -1);
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (btmesh != null && btmesh.isConnected()) {
                    btmesh.disconnectGatt(handle);
                }
                if (isGattPending) {
                    // if provisioning is pending
                    if (provisionMachine.getCurrentState() == PROVISION_CONNECT && unProvDevice != null) {
                        handler.sendEmptyMessageDelayed(REQ_CONNECT_GATT_PROV, 500);
                        return;
                    }
                    if (proxyMachine.getCurrentState() == Constants.PROXY_CONNECT && proxyDevice != null) {
                        handler.sendEmptyMessageDelayed(REQ_CONNECT_GATT_PROXY, 500);
                        return;
                    }
                }
                if(!proxyPending) {
                    proxyDevice = null;
                }

//                unProvDevice = null;


            } else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // provision is pending
                if (provisionMachine.getCurrentState() == PROVISION_CONNECT) {
                    provisionMachine.transitionTo(Constants.PROVISION_READY);
                    provisionMachine.fireEvent(Constants.EVENT_CONNECT);
                }
                if (proxyMachine.getCurrentState() == Constants.PROXY_CONNECT) {
                    Log.d(TAG, "APP - initBluetoothMeshProxy");
                    int status = btmesh.initBluetoothMeshProxy(Constants.HANDLE_PROXY, 69);
                    proxyMachine.transitionTo(PROXY_START);
                    if (status != 0) {
                        Log.e(TAG, "Error white initBluetoothMeshProxy");
                        proxyMachine.transitionTo(PROXY_READY);
                    }
                }
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                final byte[] data = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                ParcelUuid uuidExtra = intent.getParcelableExtra(Constants.EXTRA_CHARACTERISTIC);
                UUID uuid = uuidExtra.getUuid();

            } else if (Constants.ACTION_GATT_SERVICES_ERROR.equals(action)) {
                provisionMachine.transitionTo(PROVISION_INIT);
                proxyMachine.transitionTo(PROXY_INIT);
                showDialog("GATT Error, Reconnecting..", 2000);
                if (btmesh != null && btmesh.isConnected()) {
                    btmesh.disconnectGatt(handle);
                }
                connectNetwork();
            } else if (Constants.ACTION_MTU_CHANGED.equals(action)) {

            } else if (Constants.ACTION_CHARACTERISTIC_WRITE.equals(action)) {
                final byte[] data = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                ParcelUuid uuidExtra = intent.getParcelableExtra(Constants.EXTRA_CHARACTERISTIC);
                UUID uuid = uuidExtra.getUuid();
            } else if (Constants.ACTION_CHARACTERISTIC_CHANGE.equals(action)) {
                final byte[] data = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                ParcelUuid uuidExtra = intent.getParcelableExtra(Constants.EXTRA_CHARACTERISTIC);
                UUID uuid = uuidExtra.getUuid();
                if (uuid.equals(BluetoothMesh.meshUnprovisionedOutChar)
                        || uuid.equals(BluetoothMesh.meshProxyOutChar)) {
                    if (provisionMachine.getCurrentState() == PROVISION_SEND) {
                        provisionMachine.transitionTo(PROVISION_WRITE);
                    } else if (proxyMachine.getCurrentState() == PROXY_SEND) {
                        proxyMachine.transitionTo(PROXY_WRITE);
                    } else if (proxyMachine.getCurrentState() == PROXY_SET_SEND) {
                        proxyMachine.transitionTo(PROXY_SET_WRITE);
                    }
                    btmesh.write(handle, data);
                } else {
                    Log.i(TAG, "Unknown charactersistic changed UUID: " + uuid);
                }

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            String tag = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    tag = Constants.HOME_TAG;
                    break;
                case R.id.navigation_scanner:
                    if (needPermissions(getApplicationContext())) {
                        requestPermissions();
                    }
                    if (getBTStatus() & isBTEnabled) {
                        fragment = new ScannerFragment();
                        tag = Constants.SCAN_TAG;
                    } else {
                        askBTSettings();
                    }
                    break;
                case R.id.navigation_settings:
                    fragment = new SettingsFragment();
                    tag = Constants.SETTINGS_TAG;
                    break;

                case R.id.navigation_clock:
                    fragment = new ClockFragment();
                    tag = Constants.CLOCK_TAG;
                    sendOpcode("07");
                    break;
            }
            return loadFragment(fragment, tag);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                    Toast.makeText(this, "Unable to get all required permissions", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Permission has been denied by user");
                    // finish();
                    return;
                }

                break;
            default:
                Log.e(TAG, "Unexpected request code");

        }
    }

    private boolean loadFragment(Fragment fragment, String tag) {
        // switching fragment
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
            return true;
        }
        return false;
    }

    static public boolean needPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions: ");
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,};
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

    public void showProvisionSuccessDialog(final int meshAddress, final byte[] deviceUuid) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Provision Success");
        alertDialog.setMessage("Provide a name for this device");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deviceName = input.getText().toString();
                addDeviceInfo(deviceName, meshAddress, deviceUuid);
                NPDViewModel npdViewModel = ViewModelProviders.of(MainActivity.this).get(NPDViewModel.class);
                npdViewModel.deleteByMac(unProvDevice.getmac());
                proxyDevice = new ProxyDevice(unProvDevice.getmac());
                proxyPending = true;
                unProvDevice = null;
                gotoHomeScreen();
                showDialog("Connecting as Proxy...", 5000);
                handler.sendEmptyMessageDelayed(REQ_CONNECT_PROXY, 2000);
            }
        });
        showDialog("Closing", 0);
        alertDialog.show();
    }

    public void showProxySuccessDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Choose a group");

        final ArrayList<GroupInfo> groupsInfo = netInfo.groupsInfo();
        String[] groups = new String[groupsInfo.size()];
        int index = 0;
        for(GroupInfo grpInfo: groupsInfo) {
            groups[index] = grpInfo.name();
            index++;
        }
        alertDialog.setItems(groups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addRemoveGroup(deviceInfo, groupsInfo.get(which));
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = alertDialog.create();
        showDialog("Closing", 0);
        dialog.show();
    }
    public class ConnectProxy implements ScannerInterface {
        public final String TAG = ScannerFragment.class.getSimpleName();
        private static final long SCAN_TIMEOUT = 5000;

        private BleScanner bleScanner;

        public ConnectProxy(Context context) {
            bleScanner = new BleScanner(context);
            List<ScanFilter> filters = new ArrayList<>();
            ParcelUuid meshProxyServ = ParcelUuid.fromString(BluetoothMesh.meshProxyService.toString());
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(meshProxyServ).build();
            // TODO: Enable this line when testing with mesh devices
            filters.add(filter);
            networkMatchResults = new ArrayList<ScanResult>();
            identityMatchResults = new ArrayList<ScanResult>();
            bleScanner.startScanning(this, SCAN_TIMEOUT, filters);
            networkMatchResults = new ArrayList<>();
            identityMatchResults = new ArrayList<>();
        }

        @Override
        public void scanningStarted() {
            isScanning = true;
            showDialog("Scanning", 60000);
        }

        @Override
        public void scanningStopped() {
            isScanning = false;
            showDialog("Stopping scan", 3000);
        }

        @Override
        public void scanResult(ScanResult result) {
            // First go through devices which don't have DCD entries yet, they might be
            // advertising with Identity instead of Network beacons, for example right
            // after provisioning

            boolean idMatch = false;
            boolean netMatch = false;
            isScanning = false;
            ParcelUuid meshProxyServ = ParcelUuid.fromString(BluetoothMesh.meshProxyService.toString());
            // NetworkInfo netInfo = btmesh.getNetworkDB().get(0);
            if (result.getScanRecord() != null && result.getScanRecord().getServiceUuids() != null
                    && result.getScanRecord().getServiceUuids().contains(meshProxyServ)) {
                for (DeviceInfo devInfo : netInfo.devicesInfo()) {
                    if (devInfo.dcd() == null) {
                        if (btmesh.deviceIdentityMatches(result.getScanRecord().getBytes(), devInfo) >= 0) {
                            identityMatchResults.add(result);
                            idMatch = true;
                            break;
                        }
                    }
                }
                // If we already have an Identity match, it won't match the Network beacon
                if (!idMatch && btmesh.networkHashMatches(netInfo, result.getScanRecord().getBytes()) >= 0) {
                    networkMatchResults.add(result);
                    netMatch = true;
                }

                if (!idMatch && !netMatch)
                    return;
                if (proxyMachine.getCurrentState() == PROXY_INIT) {
                    proxyMachine.transitionTo(PROXY_READY);
                }
                bleScanner.stopScanning();
                proxyMachine.transitionTo(Constants.PROXY_CONNECT);
                handler.sendEmptyMessageDelayed(Constants.REQ_CONNECT_HIGH_RSSI, 1000);
            }
        }
    }

    public class MeshHandler {

        @StateHandler(state = MESH_INIT)
        public void onEventMeshInit(String event, TinyMachine tm) {
            if ("INIT".equals(event)) {
                // Create bluetooth mesh
                btmesh = new BluetoothMesh(getApplicationContext(), btmeshCallback);
                tm.fireEvent("LOAD");
            }
            if ("LOAD".equals(event)) {
                if (!btmesh.getNetworkDB().isEmpty()) {
                    netInfo = btmesh.getNetworkDB().get(0);
                    groupInfo = netInfo.groupsInfo().get(0);
                    Log.d(TAG, "Network DB contains some meshes " + netInfo.name());
                    tm.transitionTo(MESH_READY);
                } else {
                    Log.d(TAG, "No Networks in Network DB");
                }
            }
        }
    }

    public class ProvisionHandler {

        @StateHandler(state = Constants.PROVISION_READY)
        public void onProvisionStart(String event, TinyMachine tm) {
            if (Constants.EVENT_CONNECT.equals(event)) {
                NetworkInfo net = btmesh.getNetworkbyID(0);
                if (net == null && unProvDevice == null) {
                    return;
                }
                btmesh.provisionDevice(net, unProvDevice.getAdvertisement(), Constants.HANDLE_PROVISION, 69);
                provisionMachine.transitionTo(PROVISION_START);
                showDialog("Provisioning...", 5000);
            }
        }
    }

    public static class ProxyHandler {

        @StateHandler(state = Constants.PROXY_INIT)
        public void onProxyStart(String event, TinyMachine tm) {

        }
    }

    public class UnProvDevice {
        String npmac;
        String npadv;

        public UnProvDevice(String npmac, String npadv) {
            this.npadv = npadv;
            this.npmac = npmac;
        }

        String getmac() {
            return npmac;
        }

        byte[] getAdvertisement() {
            return Base64.decode(npadv, Base64.NO_WRAP);
        }
    }

    public class ProxyDevice {
        String pmac;

        public ProxyDevice(String pmac) {
            this.pmac = pmac;
        }

        String getMac() {
            return pmac;
        }
    }

    public boolean testProxyinit() {
        Log.w(TAG, "Code test flow is running for proxy");
        addDeviceInfo("test device", 1, unProvDevice.getAdvertisement());
        int status = btmesh.initBluetoothMeshProxy(Constants.HANDLE_PROXY, 69);
        if (status != 0) {
            Log.e(TAG, "Error white initBluetoothMeshProxy");
            proxyMachine.transitionTo(PROXY_READY);
            return false;
        }

        return true;
    }

    public boolean testProxygattReq(int gattHandle) {
        Log.w(TAG, "Code test flow is running for proxy gatt");
        btmesh.connectGatt(gattHandle);
        Log.d(TAG, "APP replied with DCDRequest with handle: " + gattHandle + " in 0.5 seconds");
        handler.sendEmptyMessageDelayed(REQ_DCD, 1000);
        return true;
    }

    private void selectHighestRssi() {
        BluetoothDevice bluetoothDevice;
        if (this.proxyDevice != null) {
            connectGatt(Constants.HANDLE_PROXY);
        }
        Collections.sort(this.identityMatchResults, Constants.rssiComparator);
        Collections.sort(this.networkMatchResults, Constants.rssiComparator);
        if (this.identityMatchResults.size() > 0) {
            bluetoothDevice = ((ScanResult) this.identityMatchResults.get(0)).getDevice();
        } else if (this.networkMatchResults.size() > 0) {
            bluetoothDevice = ((ScanResult) this.networkMatchResults.get(0)).getDevice();
        } else {
            bluetoothDevice = null;
        }
        if (bluetoothDevice != null) {
            this.proxyDevice = new ProxyDevice(bluetoothDevice.getAddress());
            connectGatt(Constants.HANDLE_PROXY);
        }
        this.identityMatchResults.clear();
        this.networkMatchResults.clear();
    }
}

// 59F49C07-FF45-49A7-A755-A3943AAB45DE
// 53696c61-6273-4465-762d-112def570b00
// 00001827-0000-1000-8000-00805f9b34fb
