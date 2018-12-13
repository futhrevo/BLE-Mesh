package in.hedera.reku.swimclock.utils;

import android.bluetooth.le.ScanResult;

import java.util.Comparator;
import java.util.UUID;

public class Constants {
    public static final String HOME_TAG = "HOME";
    public static final String SCAN_TAG = "SCAN";
    public static final String SETTINGS_TAG = "SETTINGS";
    public static final String CLOCK_TAG = "CLOCK_TAG";
    public static final String SCANNING = "Scanning";
    public static final int SCAN_TIMEOUT = 5;

    public static final int HANDLE_PROVISION = 1;
    public static final int HANDLE_PROXY = 2;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String ACTION_GATT_CONNECTED = "in.hedera.reku.swimclock.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "in.hedera.reku.swimclock.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "in.hedera.reku.swimclock.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "in.hedera.reku.swimclock.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "in.hedera.reku.swimclock.EXTRA_DATA";
    public static final String EXTRA_CHARACTERISTIC = "in.hedera.reku.swimclock.EXTRA_CHARACTERISTIC";
    public static final String ACTION_GATT_SERVICES_ERROR = "in.hedera.reku.swimclock.ACTION_GATT_SERVICES_ERROR";
    public static final String ACTION_MTU_CHANGED = "in.hedera.reku.swimclock.ACTION_MTU_CHANGED";
    public static final String ACTION_CHARACTERISTIC_WRITE = "in.hedera.reku.swimclock.ACTION_CHARACTERISTIC_WRITE";
    public static final String ACTION_CHARACTERISTIC_CHANGE = "in.hedera.reku.swimclock.ACTION_CHARACTERISTIC_CHANGE";
    public static final String ACTION_GATT_HANDLE = "in.hedera.reku.swimclock.ACTION_GATT_HANDLE";

    public static final int MESH_INIT = 100;
    public static final int MESH_READY = 101;
    public static final int MESH_GROUP_APPKEY = 102;

    public static final int PROXY_INIT = 200;
    public static final int PROXY_READY = 201;
    public static final int PROXY_CONNECT = 202;
    public static final int PROXY_START = 203;
    public static final int PROXY_SEND = 204;
    public static final int PROXY_WRITE = 205;
    public static final int PROXY_DCD = 206;
    public static final int PROXY_SET = 207;
    public static final int PROXY_DCD_WRITE_SEND = 208;
    public static final int PROXY_SET_SEND = 216;
    public static final int PROXY_SET_WRITE = 226;

    public static final int PROVISION_INIT = 300;
    public static final int PROVISION_CONNECT = 301;
    public static final int PROVISION_READY = 302;

    public static final int PROVISION_START = 303;
    public static final int PROVISION_SEND = 304;
    public static final int PROVISION_WRITE = 305;
    public static final int PROVISION_DISCONNECT = 306;

    public static final String EVENT_CONNECT = "CONNECT";

    public static final int REQ_DCD = -1001;
    public static final int REQ_HIDE_DIALOG = -1002;
    public static final int REQ_CONNECT_PROXY = -1010;
    public static final int REQ_CONNECT_GATT_PROXY = -1011;
    public static final int REQ_CONNECT_GATT_PROV = -1012;
    public static final int REQ_CONNECT_HIGH_RSSI = -1020;

    public static final UUID CLOCK_SERVICE = UUID.fromString("264ca5dc-3364-46df-8039-8c7b33664238");
    // data out read
    public static final UUID CLOCK_DATA_OUT = UUID.fromString("fca84d2f-d134-4e33-a53a-99c4b7ec7219");
    // data in write without response
    public static final UUID CLOCK_DATA_IN = UUID.fromString("f8c82543-fb49-4234-a193-e105344c585f");

    public static final boolean mock = false;


    public static byte[][] sliceWrite(byte[] writeArray, int mtuSize) {
        int len = writeArray.length;
        int chunks = (int) Math.ceil(len / (double) mtuSize);
        byte[][] ret = new byte[chunks][];

        int start = 0;
        for (int i = 0; i < chunks; i++) {
            if (start + mtuSize > len) {
                ret[i] = new byte[len - start];
                System.arraycopy(writeArray, start, ret[i], 0, len - start);

            } else {
                ret[i] = new byte[mtuSize];
                System.arraycopy(writeArray, start, ret[i], 0, mtuSize);
            }

            start += mtuSize;
        }
        return ret;
    }

    public static Comparator<ScanResult> rssiComparator = new Comparator<ScanResult>() {
        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            return Integer.compare(rhs.getRssi(), lhs.getRssi());
        }
    };
}
