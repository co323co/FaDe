package com.example.fade;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

public class DAO {}

//인터페이스로 선언해야 함
@Dao
interface PersonDAO {
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

@Dao
interface GroupDAO {
    @Query("SELECT * FROM `Group`")
    List<Group> getAll();

    @Insert
    void insertAll(Group... group);

    @Insert
    void insert(Group group);

    @Update
    void  update(Group person);

    @Query("DELETE  FROM `Group` WHERE gid = :gid")
    void deleteById(int gid);

    @Delete
    void delete(Group group);
}
