package com.example.fade.Server;

//사진인식 후 서버에서 받아올 데이터 형태임
public class DetData {

    private final int gid;

    public DetData(int gid) {
        this.gid = gid;
    }

    public int getGid() {
        return gid;
    }
}