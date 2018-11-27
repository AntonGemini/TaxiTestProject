package com.sassaworks.taxitestproject.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

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
}
