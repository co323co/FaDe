package com.example.fade;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Person {

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
