package com.example.fade.Server;

public class PersonData {

    private int pid;
    private String name;
    private byte[] thumbnail;

    //JSON key 문자열이랑 변수명랑 이름이 같아야함
    public PersonData(int pid, String name, byte[] thumbnail) {
        this.pid=pid;
        this.name=name;
        this.thumbnail=thumbnail;
    }

    public int getPid(){ return pid;}
    public String getName(){ return name;}
    public byte[] getThumbnail(){ return thumbnail;}
}
