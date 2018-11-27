package com.sassaworks.taxitestproject.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface LocationRouteDao {

    @Query("SELECT * FROM route order by created_at")
    List<LocationRoute> loadAllRoutes();

    @Insert
    void insertRoute(LocationRoute locationRoute);

    @Delete
    void deleteRoute(LocationRoute locationRoute);

}
