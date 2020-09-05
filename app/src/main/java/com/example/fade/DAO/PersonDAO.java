package com.example.fade.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.fade.Converters;
import com.example.fade.entity.Person;

import java.util.List;

//인터페이스로 선언해야 함
@Dao
public interface PersonDAO {
    @Query("SELECT * FROM Person")
    List<Person> getAll();

    @Query("SELECT * FROM Person WHERE pid in (:pidList)")
    List<Person> getByIdList(List<Integer> pidList);

    @Query("SELECT max(pid) FROM Person")
    int getRecentlyPID();

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
