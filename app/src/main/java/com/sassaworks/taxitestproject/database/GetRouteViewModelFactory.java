package com.sassaworks.taxitestproject.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class GetRouteViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final String mRoute;

    public GetRouteViewModelFactory(AppDatabase db, String route) {
        mDb = db;
        mRoute = route;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GetRouteViewModel(mDb, mRoute);
    }
}
