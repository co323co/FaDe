package com.example.fade;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//추상클래스
@Database(entities = {Person.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    //호출하면 DAO를 반환함
    public abstract PersonDAO personDAO();

    //싱글턴화
    private static AppDatabase INSTANCE;
    private  static  final Object sLock = new Object();
    public static  AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Person.db").build();
            }
            return INSTANCE;
        }
    }
}

