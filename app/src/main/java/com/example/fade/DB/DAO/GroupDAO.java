package com.example.fade.DB.DAO;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.fade.DB.Converters;
import com.example.fade.DB.entity.Group;

import java.util.List;

@TypeConverters({Converters.class})
@Dao
public interface GroupDAO {

    @Query("SELECT personIdList FROM `GROUP` WHERE gid = :gid")
    String getPidList(int gid);

    @Insert
    void insert(Group group);

    @Update
    void  update(Group group);

    @Query("SELECT name FROM `Group` WHERE gid = (:gidList)")
    List<String> getGnameList(int gidList);

    @Delete
    void delete(Group group);


    @Query("SELECT gid FROM `GROUP`")
    List<Integer> getGidList();

    // ||는 문자열 연결 연산임
    @Query("SELECT gid FROM `Group` WHERE  personIdList LIKE '%,' || :pid || ',%'")
    List<Integer> getGidListByPid(String pid);

}