package in.hedera.reku.swimclock;

import android.app.Application;

import com.facebook.stetho.Stetho;

import in.hedera.reku.swimclock.utils.FileLoggingTree;
import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        Timber.plant(new FileLoggingTree(getApplicationContext()));
    }
}
