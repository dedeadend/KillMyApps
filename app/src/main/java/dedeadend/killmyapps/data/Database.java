package dedeadend.killmyapps.data;

import androidx.room.RoomDatabase;

import dedeadend.killmyapps.model.PKGName;

@androidx.room.Database(entities = {PKGName.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    public abstract excludedPkgDao excludedPkgDao();
}
