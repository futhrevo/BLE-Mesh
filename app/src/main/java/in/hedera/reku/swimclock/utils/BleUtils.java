package in.hedera.reku.swimclock.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import in.hedera.reku.swimclock.BluetoothLeService;

/**
 * Created by rakeshkalyankar on 22/09/18.
 */
public class BleUtils {

    private final static String TAG = BleUtils.class.getSimpleName();

    public static boolean hasBluetoothLE(Context context) {
        return (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }


    public static void readCharacteristic(UUID enableUuid, String name,
                                          List<BluetoothGattCharacteristic> gattCharacteristics,
                                          BluetoothLeService mBluetoothLeService) {
        BluetoothGattCharacteristic gattCharacteristic = findCharacteristic(
                enableUuid, gattCharacteristics);
        if (gattCharacteristic != null) {
            Log.d(TAG, "Read " + name);
            mBluetoothLeService.readCharacteristic(gattCharacteristic);
        }
    }

    public static void writeCharacteristic(UUID enableUuid, String name,
                                           List<BluetoothGattCharacteristic> gattCharacteristics,
                                           BluetoothLeService mBluetoothLeService, byte[] value) {
        BluetoothGattCharacteristic gattCharacteristic = findCharacteristic(
                enableUuid, gattCharacteristics);
        if (gattCharacteristic != null) {
            Log.d(TAG, "Write " + name);
            gattCharacteristic.setValue(value);
            gattCharacteristic
                    .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mBluetoothLeService.writeCharacteristic(gattCharacteristic);
        }
    }

    public static BluetoothGattCharacteristic findCharacteristic(UUID uuid,
                                                                 List<BluetoothGattCharacteristic> gattCharacteristics) {
        if (gattCharacteristics != null && gattCharacteristics.size() > 0) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic != null) {
                    if (gattCharacteristic.getUuid().equals(uuid)) {
                        return gattCharacteristic;
                    }
                }
            }
        }
        return null;
    }

    public static BluetoothGattService findService(UUID uuid,
                                                   List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return null;
        for (BluetoothGattService gattService : gattServices) {
            if (uuid.equals(gattService.getUuid())) {
                return gattService;
            }
        }
        return null;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
