package in.hedera.reku.swimclock;

/**
 * Created by rakeshkalyankar on 23/09/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public interface FragListener {
    // Home Fragment
    void createNetwork(String name);

    // Scan Fragment
    void startProvision(String mac);

    void deleteNetwork();
}
