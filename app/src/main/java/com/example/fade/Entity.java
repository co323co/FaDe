package com.example.fade;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class Entity {}

@androidx.room.Entity
class Person {

    @PrimaryKey(autoGenerate=true)
    private int pid;
    @ColumnInfo
    private String name;

    public Person(String name){
        this.name=name;
    }

    public int getPid() {return  pid;}
    public void setPid(int pid) {this.pid=pid;}
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
}

@androidx.room.Entity
class Group {

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