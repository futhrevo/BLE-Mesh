package in.hedera.reku.swimclock.utils;

import java.security.SecureRandom;

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

    // byte[] with size 16
    public static byte[] createNetKey() {
        byte[] randomKey = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(randomKey);
        return randomKey;
    }

    public static byte[] createAppKey() {
        byte[] randomKey = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(randomKey);
        return randomKey;
    }

}
