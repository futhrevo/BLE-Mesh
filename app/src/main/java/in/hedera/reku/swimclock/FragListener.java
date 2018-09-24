package in.hedera.reku.swimclock;

import com.silabs.bluetooth_mesh.DeviceInfo;

/**
 * Created by rakeshkalyankar on 23/09/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public interface FragListener {
    // Home Fragment
    void createNetwork(String name);
    void createGroup(String name);
    void readNetInfo();
    void gotoScannerScreen();
    void setProxy(DeviceInfo deviceInfo);
    void setRelay(DeviceInfo deviceInfo);

    // Scan Fragment
    void startProvision(String mac, String advertisement);

    void deleteNetwork();

    void disableUIinteraction(boolean on);
}
