package com.example.fade;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//인터페이스로 선언해야 함
@Dao
public interface PersonDAO {
    @Query("SELECT * FROM Person")
    List<Person> getAll();

    @Insert
    void insertAll(Person... people);

    @Insert
    void insert(Person person);

    @Update
    void  update(Person person);

    @Query("DELETE  FROM Person WHERE pid = :pid")
    void deleteById(int pid);

    @Delete
    void delete(Person person);
}

