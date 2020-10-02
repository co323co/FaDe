package com.example.fade.Server;

public class GroupData {

    private int id;
    private String name;
    private int favorites;

    //JSON key 문자열이랑 변수명랑 이름이 같아야함
    public GroupData(int id, String name, int favorites) {
        this.id=id;
        this.name=name;
        this.favorites=favorites;
    }

    public int getId(){ return id;}
    public String getName(){ return name;}
    public int getFavorites(){ return favorites;}

    public void setId(int id) {this.id=id;}
    public void setName(String name) {this.name=name;}
    public void setFavorites(int favorites) {this.favorites=favorites;}

}
