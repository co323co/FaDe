package com.example.fade.DB.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.fade.DB.Converters;

import java.util.ArrayList;

@TypeConverters(Converters.class)
@Entity
public class Group {

    @PrimaryKey(autoGenerate = true)
    private int gid;

    @ColumnInfo
    private String name;

    @TypeConverters({Converters.class})
    @ColumnInfo(name = "personIdList")
    private ArrayList<Integer> personIDList = new ArrayList();

    @ColumnInfo
    private int favorites=0;

    public Group() { }

    @Ignore
    public Group(String name) {
        this.name = name;
    }

    @Ignore
    public Group(ArrayList<Integer> personIDList) {
        this.personIDList = personIDList;
    }

    public Group(String name, ArrayList<Integer> personIDList) {
        this.name = name;
        this.personIDList = personIDList;
    }

    public int getGid() { return gid; }
    public void setGid(int gid) { this.gid = gid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArrayList<Integer> getPersonIDList() { return personIDList; }
    public void setPersonIDList(ArrayList<Integer> personIDList) { this.personIDList = personIDList; }

    public int getFavorites() {return favorites;}
    public void setFavorites(int favorites){this.favorites = favorites;}

    public void copy(Group group){
            group.setGid(gid);
            group.setName(name);
            group.setPersonIDList(personIDList);
    }

}

