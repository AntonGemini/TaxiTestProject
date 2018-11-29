package com.sassaworks.taxitestproject.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class GetRouteViewModel extends ViewModel {

    private LiveData<List<LocationRoute>> mRoute;

    public GetRouteViewModel(AppDatabase db, String route)
    {
        mRoute = db.routeDao().loadRouteByName(route);
    }

    public LiveData<List<LocationRoute>> getRoute()
    {
        return mRoute;
    }

}
