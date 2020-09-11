package com.example.fade.DB.DAO;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.fade.DB.Converters;
import com.example.fade.DB.entity.Group;

import java.util.ArrayList;
import java.util.List;

@TypeConverters({Converters.class})
@Dao
public interface GroupDAO {
    @Query("SELECT * FROM `Group`")
    List<Group> getAll();

    //보통 (아마도) SELECT 컬럼A를 하면  테이블에서 컬럼A만채워지고 나머진 빈 형태로 넘어오기때문에, 반환값 데이터타입이 안맞을 수도 있음
    //프로그래머 입장에선 당연히 하나여도 컴퓨터 입장에서 결과가 여러개일 수도 있는 경우에는 리스트로 반환됨.
    // ex) String <-> ArrayList로 컨버터를 만들어둔 경우에, 결과가 여러개일 수 있으면 List<String>으로 반환되어 컨버터가 작동을 못함
    //Count(*)처럼 그룹바이라 당연히 하나인 경우에는 아마 잘 작동하는 듯
    //List<String>가 반환된 경우에 반환형을 String으로 하면 자동으로 잘려서 맨 앞만 사용됨
    @Query("SELECT personIdList FROM `GROUP` WHERE gid = :gid")
    String getPidList(int gid);

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



    @Query("SELECT name FROM `Group` WHERE gid in (:gidList)")
    List<String> getGnameList(ArrayList<Integer> gidList);

    @Delete
    void delete(Group group);


}