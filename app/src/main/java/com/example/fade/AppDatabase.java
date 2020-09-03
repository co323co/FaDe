package com.example.fade;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.example.fade.entity.*;
import com.example.fade.DAO.*;
import java.util.ArrayList;
import java.util.List;


//추상클래스
@Database(entities = {Person.class, Group.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    //호출하면 DAO를 반환함
    public abstract PersonDAO personDAO();
    public abstract GroupDAO groupDAO();

    //싱글턴화
    private static AppDatabase INSTANCE;
    private  static  final Object sLock = new Object();
    public static  AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "App.db").build();
            }
            return INSTANCE;
        }
    }
}
