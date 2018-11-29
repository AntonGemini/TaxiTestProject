package com.sassaworks.taxitestproject.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;


@Dao
public interface LocationRouteDao {

    @Query("SELECT * FROM location order by created_at")
    LiveData<List<LocationRoute>> loadAllRoutes();

    @Insert
    void insertRoute(LocationRoute locationRoute);

    @Delete
    void deleteRoute(LocationRoute locationRoute);

    @Query("SELECT name, created_at, COUNT(*) as cnt FROM location GROUP BY name")
    LiveData<List<TempLocal>> loadGroupedRoutes();

    @Query("DELETE FROM location WHERE name=:name")
    void deleteByName(String name);

    @Query("SELECT * FROM location WHERE name=:name order by id")
    LiveData<List<LocationRoute>> loadRouteByName(String name);




    static class TempLocal {
        String name;
        Date created_at;
        Long cnt;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getCreated_at() {
            return created_at;
        }

        public void setCreated_at(Date created_at) {
            this.created_at = created_at;
        }

        public Long getCnt() {
            return cnt;
        }

        public void setCnt(Long cnt) {
            this.cnt = cnt;
        }
    }


}
