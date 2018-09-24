package in.hedera.reku.swimclock.utils;

public class Constants {
    public static final String HOME_TAG = "HOME";
    public static final String SCAN_TAG = "SCAN";
    public static final String SETTINGS_TAG = "SETTINGS";
    public static final String SCANNING =  "Scanning" ;
    public static final long SCAN_TIMEOUT = 5000;

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

    public static final int MESH_INIT = 100;
    public static final int MESH_READY = 101;
    public static final int MESH_GROUP_APPKEY = 102;

    public static final int PROXY_INIT = 200;
    public static final int PROXY_READY = 201;
    public static final int PROXY_START = 202;
    public static final int PROXY_SEND = 203;
    public static final int PROXY_WRITE = 204;
    public static final int PROXY_DCD = 205;
    public static final int PROXY_SET = 206;
    public static final int PROXY_SET_SEND = 216;
    public static final int  PROXY_SET_WRITE = 226;

    public static final int PROVISION_INIT = 300;
    public static final int PROVISION_START = 301;
    public static final int PROVISION_SEND = 302;
    public static final int PROVISION_WRITE = 303;
    public static final int PROVISION_DISCONNECT = 304;
    public static final int PROVISION_READY = 305;

}
