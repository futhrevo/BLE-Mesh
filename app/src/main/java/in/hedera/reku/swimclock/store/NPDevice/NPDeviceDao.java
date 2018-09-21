package in.hedera.reku.swimclock.store.NPDevice;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface NPDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NPDevice npDevice);

    @Query("DELETE FROM scanned_devices")
    void deleteAll();

    @Query("SELECT * FROM scanned_devices ORDER BY rssi DESC")
    LiveData<List<NPDevice>>  getAllScannedDevices();

    @Query("DELETE FROM scanned_devices WHERE mac = :mac")
    void deleteDeviceByMac(String mac);
}
