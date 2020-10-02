package com.example.fade.Server;

import android.util.Base64;

public class PersonData {

    private int id;
    private String name;
    private byte[] thumbnail;

    //JSON key 문자열이랑 변수명랑 이름이 같아야함
    public PersonData(int id, String name, byte[] thumbnail) {
        this.id=id;
        this.name=name;
        this.thumbnail=thumbnail;
    }

    public int getId(){ return id;}
    public String getName(){ return name;}
    public byte[] getThumbnail(){
        if(thumbnail == null) return null;
        //서버에 byte[] -> String으로 저장했었음으로 받아올 때는 다시 byte[]로 변환해준다.
        return Base64.decode(thumbnail, Base64.NO_WRAP);
    }

    public void setId(int id) {this.id=id;}
    public void setName(String name) {this.name=name;}
    public void setThumbnail(byte[] thumbnail) {this.thumbnail=thumbnail;}
}
