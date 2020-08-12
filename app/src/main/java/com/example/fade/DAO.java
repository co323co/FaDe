package com.example.fade;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.fade.entity.Group;
import com.example.fade.entity.Person;

import java.util.ArrayList;
import java.util.List;

public class DAO {}

//인터페이스로 선언해야 함
@Dao
interface PersonDAO {
    @Query("SELECT * FROM Person")
    List<Person> getAll();

    //TODO::사이즈 0만 반환함
    @Query("SELECT * FROM Person WHERE pid in (SELECT personIDList FROM `Group` WHERE gid = :gid )")
    List<Person> getByIdList(int gid);

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

@TypeConverters({Converters.class})
@Dao
interface GroupDAO {
    @Query("SELECT * FROM `Group`")
    List<Group> getAll();

    @Query("SELECT * FROM `GROUP` WHERE gid = :gid")
    Group get(int gid);

    @Query("SELECT count(*) FROM `GROUP`")
    int getCount();

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