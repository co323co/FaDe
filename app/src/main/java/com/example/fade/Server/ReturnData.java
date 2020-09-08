package com.example.fade.Server;

import java.util.ArrayList;

public class ReturnData {

    private String db0, db1, db2;
    private boolean result;


    //JSON key 문자열이랑 변수명랑 이름이 같아야함
    public ReturnData(String  db0, String db1, String db2, boolean result) {
        this.db0=db0;
        this.db1=db1;
        this.db2=db2;
        this.result=result;
    }
    public ArrayList<String> getDB(){
        ArrayList<String> dbFileList=new ArrayList<>();
        if(db0!=null)
        dbFileList.add(db0);
        dbFileList.add(db1);
        dbFileList.add(db2);
        return dbFileList;
    }
    public boolean getResult(){
        return result;
    }
}