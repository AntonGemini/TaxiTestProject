package com.sassaworks.taxitestproject.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;


@Dao
public interface LocationRouteDao {

    @Query("SELECT * FROM route order by created_at")
    List<LocationRoute> loadAllRoutes();

    @Insert
    void insertRoute(LocationRoute locationRoute);

    @Delete
    void deleteRoute(LocationRoute locationRoute);

}
