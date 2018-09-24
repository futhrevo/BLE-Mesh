package in.hedera.reku.swimclock;

/**
 * Created by rakeshkalyankar on 23/09/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public interface FragListener {
    // Home Fragment
    void createNetwork(String name);
    void readNetInfo();
    void gotoScannerScreen();

    // Scan Fragment
    void startProvision(String mac, String advertisement);

    void deleteNetwork();

    void disableUIinteraction(boolean on);
}
