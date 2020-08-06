package com.example.fade;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroupDAO {
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
