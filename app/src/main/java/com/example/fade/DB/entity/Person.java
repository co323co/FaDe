package com.example.fade.DB.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Person {

    @PrimaryKey(autoGenerate = true)
    private int pid;
    @ColumnInfo
    private String name;
    private byte[] profile_picture;

    @Ignore
    public Person() { }

//    public Person(String name) {
//        this.name = name;
//    }

    public Person(String name, byte[] profile_picture) {
        this.name = name;
        this.profile_picture = profile_picture;
    }

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

    public byte[] getProfile_picture(){
        return profile_picture;
    }
    public void setProfile_picture(byte[] profile_picture){
        this.profile_picture = profile_picture;
    }
}