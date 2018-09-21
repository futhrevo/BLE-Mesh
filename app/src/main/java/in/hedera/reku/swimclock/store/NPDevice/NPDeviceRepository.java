package in.hedera.reku.swimclock.store.NPDevice;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import in.hedera.reku.swimclock.store.MeshDatabase;

public class NPDeviceRepository {

    private NPDeviceDao npDeviceDao;
    private LiveData<List<NPDevice>> allNPdevices;

    public NPDeviceRepository(Application application) {
        MeshDatabase db = MeshDatabase.getDatabase(application);
        npDeviceDao = db.npDeviceDao();
        allNPdevices = npDeviceDao.getAllScannedDevices();
    }

    public LiveData<List<NPDevice>> getAllScannedDevices() {
        return allNPdevices;
    }

    public void insert (NPDevice npDevice) {
        new insertAsyncTask(npDeviceDao).execute(npDevice);
    }

    public void deleteAll() {
        new deleteAllAsyncTask(npDeviceDao).execute();
    }

    public void deleteByMac(String mac) {
        new deleteAsyncTask(npDeviceDao).execute(mac);
    }

    private static class insertAsyncTask extends AsyncTask<NPDevice, Void, Void> {

        private NPDeviceDao asyncTaskDao;

        insertAsyncTask( NPDeviceDao dao) {
            asyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(final NPDevice... npDevices) {
            asyncTaskDao.insert(npDevices[0]);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private NPDeviceDao asyncTaskDao;
        deleteAllAsyncTask( NPDeviceDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            asyncTaskDao.deleteAll();
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<String, Void, Void> {

        private NPDeviceDao asyncTaskDao;
        deleteAsyncTask( NPDeviceDao dao) {
            asyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(String... params) {
            asyncTaskDao.deleteDeviceByMac(params[0]);
            return null;
        }
    }
}
