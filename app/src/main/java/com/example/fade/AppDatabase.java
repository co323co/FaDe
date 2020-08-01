package com.example.fade;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//추상클래스
@Database(entities = {Person.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    //호출하면 DAO를 반환함
    public abstract PersonDAO personDAO();
    //TODO::싱글턴화 할것
}
