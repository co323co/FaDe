package com.example.fade.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    String uid;

    public User(String uid) {this.uid=uid;}

    public String getUid(){return uid;}
    public void setUid(String uid){this.uid=uid;}

}
