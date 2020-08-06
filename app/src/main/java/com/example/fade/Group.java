package com.example.fade;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Group {

    @PrimaryKey(autoGenerate=true)
    private int gid;
    @ColumnInfo
    private String name;

    public Group(String name){
        this.name=name;
    }

    public int getGid() {return  gid;}
    public void setGid(int gid) {this.gid=gid;}
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}

}