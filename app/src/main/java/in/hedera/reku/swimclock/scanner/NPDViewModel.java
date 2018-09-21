package in.hedera.reku.swimclock.scanner;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import in.hedera.reku.swimclock.store.NPDevice.NPDevice;
import in.hedera.reku.swimclock.store.NPDevice.NPDeviceRepository;

public class NPDViewModel extends AndroidViewModel {

    private NPDeviceRepository npDeviceRepository;

    private LiveData<List<NPDevice>> allNPdevices;


    public NPDViewModel(@NonNull Application application) {
        super(application);
        npDeviceRepository = new NPDeviceRepository(application);
        allNPdevices = npDeviceRepository.getAllScannedDevices();
    }

    LiveData<List<NPDevice>> getAllNPdevices() {
        return allNPdevices;
    }

    public void insert(NPDevice npDevice) {
        npDeviceRepository.insert(npDevice);
    }

    public void deleteAll() {
        npDeviceRepository.deleteAll();
    }

    public void deleteByMac(String mac) {
        npDeviceRepository.deleteByMac(mac);
    }
}
