package com.example.fade.Server;

public class RegiData {

    private final String uid;
    private final int pid;

    public RegiData(String uid, int pid) {
        this.uid = uid;
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public int getPid() {
        return pid;
    }
}