package com.example.fade;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Group.class}, version = 1)
public abstract class GroupDatabase extends RoomDatabase {

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
