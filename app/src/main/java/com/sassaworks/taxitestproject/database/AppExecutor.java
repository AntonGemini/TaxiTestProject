package com.sassaworks.taxitestproject.database;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static final Object LOCK = new Object();
    private static AppExecutor sInstance;
    private final Executor dbExecutor;

    private AppExecutor(Executor dbExecutor)
    {
        this.dbExecutor = dbExecutor;
    }

    public static AppExecutor getInstance()
    {
        if (sInstance==null)
        {
            synchronized (LOCK) {
                sInstance = new AppExecutor(Executors.newSingleThreadExecutor());
            }
        }
        return sInstance;
    }

    public Executor getDbExecutor() {return dbExecutor;}


}
