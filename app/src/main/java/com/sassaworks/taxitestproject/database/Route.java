package com.sassaworks.taxitestproject.database;

import android.arch.persistence.room.Entity;

import java.util.Date;

@Entity(tableName = "route")
public class Route {

    private String route;
    private Long count;
    private Date createdAt;

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
