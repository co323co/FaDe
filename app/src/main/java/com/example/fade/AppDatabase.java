package com.example.fade;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

public  class AppDatabase{}

//Person DB임
//추상클래스
@Database(entities = {Person.class}, version = 1)
abstract class PersonDatabase extends RoomDatabase {

    //호출하면 DAO를 반환함
    public abstract PersonDAO personDAO();

    //싱글턴화
    private static PersonDatabase INSTANCE;
    private  static  final Object sLock = new Object();
    public static  PersonDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(), PersonDatabase.class, "Person.db").build();
            }
            return INSTANCE;
        }
    }
}

//Group DB임
@Database(entities = {Group.class}, version = 1)
abstract class GroupDatabase extends RoomDatabase {

    //호출하면 DAO를 반환함
    public abstract GroupDAO groupDAO();

    //싱글턴화
    private static GroupDatabase INSTANCE;
    private  static  final Object sLock = new Object();
    public static  GroupDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(), GroupDatabase.class, "Group.db").build();
            }
            return INSTANCE;
        }
    }
}
