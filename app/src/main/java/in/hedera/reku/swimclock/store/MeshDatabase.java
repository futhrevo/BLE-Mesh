package in.hedera.reku.swimclock.store;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import in.hedera.reku.swimclock.store.NPDevice.NPDevice;
import in.hedera.reku.swimclock.store.NPDevice.NPDeviceDao;

@Database(entities = {NPDevice.class}, version = 1)
public abstract class MeshDatabase extends RoomDatabase {

    public abstract NPDeviceDao npDeviceDao();

    private static volatile MeshDatabase INSTANCE;

    public static MeshDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (MeshDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MeshDatabase.class, "mesh_database").build();
                }
            }
        }
        return INSTANCE;
    }
    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }
}
