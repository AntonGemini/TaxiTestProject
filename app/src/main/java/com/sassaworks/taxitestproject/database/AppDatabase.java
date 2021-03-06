package com.sassaworks.taxitestproject.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;



@Database(entities = {LocationRoute.class}, version = 1,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance;
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "taxiTest";

    public static AppDatabase getInstance(Context context)
    {
        if (sInstance == null)
        {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context,AppDatabase.class,DATABASE_NAME).build();
            }
        }
        return sInstance;
    }

    public abstract LocationRouteDao routeDao();
}
