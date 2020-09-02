package com.example.fade.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = User.class, parentColumns = "uid", childColumns = "uid"))
public class Person {
    @ColumnInfo
    private String uid;
    @PrimaryKey(autoGenerate = true)
    private int pid;
    @ColumnInfo
    private String name;

    @Ignore
    public Person() { }

    public Person(String name) {
        this.name = name;
    }

    public String getUid() {return  uid;}
    public void setUid(String uid) {this.uid=uid;}

    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}