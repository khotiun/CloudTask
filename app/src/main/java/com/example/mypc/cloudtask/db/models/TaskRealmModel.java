package com.example.mypc.cloudtask.db.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hotun on 23.09.2017.
 */

public class TaskRealmModel extends RealmObject {
    @PrimaryKey
    private long id;
    private String title;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
