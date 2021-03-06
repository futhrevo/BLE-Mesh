package in.hedera.reku.swimclock;

import android.support.v4.app.Fragment;

import com.silabs.bluetooth_mesh.DeviceInfo;
import com.silabs.bluetooth_mesh.GroupInfo;

/**
 * Created by rakeshkalyankar on 23/09/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public interface FragListener {
    // Home Fragment
    void createNetwork(String name);
    void createGroup(String name);
    void readNetInfo(Fragment frag);
    void gotoScannerScreen();
    void gotoHomeScreen();
    void setProxy(DeviceInfo deviceInfo);
    void setRelay(DeviceInfo deviceInfo);
    void connectNetwork();

    // Scan Fragment
    void startProvision(String mac, String advertisement);

    void deleteNetwork();

    void disableUIinteraction(boolean on);

    void addRemoveGroup(DeviceInfo deviceInfo, GroupInfo grpInfo);

    void onOffSet(DeviceInfo deviceInfo, GroupInfo groupInfo);

    void factoryReset(DeviceInfo deviceInfo);

    void sendOpcode(String opcode);

}
