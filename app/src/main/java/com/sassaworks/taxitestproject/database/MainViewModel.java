package com.sassaworks.taxitestproject.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<LocationRoute>> mRoutes;
    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(this.getApplication());
        mRoutes = db.routeDao().loadAllRoutes();
    }

    public LiveData<List<LocationRoute>> getRoutes()
    {
        return mRoutes;
    }
}
