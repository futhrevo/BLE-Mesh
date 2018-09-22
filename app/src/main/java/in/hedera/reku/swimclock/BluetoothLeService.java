package in.hedera.reku.swimclock;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.silabs.bluetooth_mesh.BluetoothMesh;

import java.util.List;

import in.hedera.reku.swimclock.utils.BleQueue;

import static in.hedera.reku.swimclock.utils.Constants.STATE_CONNECTED;
import static in.hedera.reku.swimclock.utils.Constants.STATE_CONNECTING;
import static in.hedera.reku.swimclock.utils.Constants.STATE_DISCONNECTED;

/**
 * Created by rakeshkalyankar on 22/09/18.
 */

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private BleQueue bleQueue;


    // Implements callback methods for GATT events that the app cares about. For
    // example, connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mConnectionState = STATE_CONNECTED;
                    Log.i(TAG, "Connected to GATT server.");
                    if (gatt.discoverServices()) {
                        // successfully started discovering
                    } else {
                        // already disconnected
                    }
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    disconnect();
                }
            } else {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Gatt server error, status: " + status + ", newState: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            bleQueue.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            bleQueue.requestMtu(69);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            bleQueue.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            bleQueue.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            bleQueue.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            bleQueue.onMtuChanged(gatt, mtu, status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onMtuChanged");
                gatt.discoverServices();
            } else {
                Log.d(TAG, "onMtuChanged status : " + status + ", mtu : " + mtu);
                gatt.requestMtu(69);
            }
        }
    };

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,"BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        bleQueue = new BleQueue(mBluetoothGatt);
        Log.d(TAG, "Trying to create a new connection.");
        //mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bleQueue.readCharacteristic(characteristic);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bleQueue.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (descriptor != null) {
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bleQueue.writeDescriptor(descriptor);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicIndication(
            BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (descriptor != null) {
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            bleQueue.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Mesh provisioning data out
     *  Unprovisioned app sends provisioner PDUs as LE notifications
     *
     * @return true if characteristic indications request is queued else false
     *
     */
    public boolean enableProvisionOutNotification() {
        if (mBluetoothGatt == null)
            return false;
        BluetoothGattService btServ = mBluetoothGatt.getService(BluetoothMesh.meshUnprovisionedService);
        // check mesh service
        if(btServ == null) {
            return false;
        }
        BluetoothGattCharacteristic btChar = btServ.getCharacteristic(BluetoothMesh.meshUnprovisionedOutChar);
        // check mesh char
        if(btChar == null) {
            return false;
        }
        BluetoothGattDescriptor descriptor = btChar.getDescriptor(BluetoothMesh.meshOutCharDescriptor);
        // check mesh descriptor
        if(descriptor == null) {
            return false;
        }
        setCharacteristicIndication(btChar, descriptor, true);
        return true;
    }

    /**
     * Mesh provisioning data in
     * Provisioner sends provisioning PDUs to unprovisioned as LE writes
     * @param gattHandle default 0
     * @param writeArray provision data
     * @return true if characteristic write was queued else false
     */
    public boolean writeProvisionInChar(int gattHandle, byte[] writeArray) {
        if(mBluetoothGatt == null) {
            Log.e(TAG, "writeGattChar: Device not connected");
            return false;
        }
        BluetoothGattService meshService = mBluetoothGatt.getService(BluetoothMesh.meshUnprovisionedService);
        if(meshService == null) {
            Log.e(TAG, "writeGattChar: Device does not contain meshService");
            return false;
        }
        BluetoothGattCharacteristic meshWrite = meshService.getCharacteristic(BluetoothMesh.meshUnprovisionedInChar);
        if(meshWrite == null) {
            Log.e(TAG, "writeGattChar : Device does not contain meshInCharacteristic");
            return false;
        }
        meshWrite.setValue(writeArray);
        meshWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharacteristic(meshWrite);
        return true;
    }

    /**
     * mesh proxy data out
     * set notification to the outchar of mesh proxy data out
     * @return true if characteristic notification enabled else false
     */
    public boolean proxyOutNotification() {
        if(mBluetoothGatt == null) {
            return false;
        }
        BluetoothGattService btServ = mBluetoothGatt.getService(BluetoothMesh.meshProxyService);
        if(btServ == null) {
            return false;
        }
        BluetoothGattCharacteristic btChar = btServ.getCharacteristic(BluetoothMesh.meshProxyOutChar);
        if(btChar == null) {
            return false;
        }
        BluetoothGattDescriptor descriptor = btChar.getDescriptor(BluetoothMesh.meshOutCharDescriptor);
        if(descriptor == null) {
            return false;
        }
        setCharacteristicNotification(btChar, descriptor, true);
        return true;
    }

    /**
     * Mesh proxy data in
     * write proxy characteristic to mesh proxy data in
     * @param gattHandle default 0
     * @param writeArray proxy data
     * @return true if characteristic write was queued else false
     */
    public boolean writeProxyInChar(int gattHandle, byte[] writeArray) {
        if(mBluetoothGatt == null) {
            Log.e(TAG,"writeGattChar : Device is not connected");
            return false;
        }
        BluetoothGattService meshService = mBluetoothGatt.getService(BluetoothMesh.meshProxyService);
        if(meshService == null) {
            Log.e(TAG, "writeGattChar : Device does not contain meshProxyService");
            return false;
        }
        BluetoothGattCharacteristic meshWrite = meshService.getCharacteristic(BluetoothMesh.meshProxyInChar);
        if(meshWrite == null) {
            Log.e(TAG, "writeGattChar : Device does not contain meshProxyInCharacteristic");
            return false;
        }
        meshWrite.setValue(writeArray);
        meshWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharacteristic(meshWrite);
        return true;
    }

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }

    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disconnect();
        close();
    }
}
