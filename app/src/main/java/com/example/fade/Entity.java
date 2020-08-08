package com.example.fade;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

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
    @ColumnInfo
    private ArrayList<Integer> personIDList=new ArrayList<Integer>();

    public Group(String name){
        this.name=name;
    }

    public int getGid() {return  gid;}
    public void setGid(int gid) {this.gid=gid;}
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
    public ArrayList<Integer> getPersonIDList() {return personIDList;}
    public void setPersonIDList(ArrayList<Integer> personIDList) {this.personIDList=personIDList;}
}

class Converters {
    @TypeConverter
    public static ArrayList<Integer> StringToIntList(String value) {
        if(value.equals("")) return new ArrayList<Integer>();

        String str[] = value.split(",");
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<str.length; i++) list.add(Integer.parseInt(str[i]));
        return list;
    }

    @TypeConverter
    public static String IntListToString(ArrayList<Integer> list) {
        String str="";
        for(int i=0; i<list.size();i++) str+=(list.get(i)).toString()+",";
        return str;
    }
}
