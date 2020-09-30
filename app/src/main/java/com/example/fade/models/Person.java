package com.example.fade.models;

public class Person {
    int id;
    String name;
    byte[] thumbnail;

    public Person(int id, String name, byte[] thumbnail)
    {
        this.id=id;
        this.name=name;
        this.thumbnail=thumbnail;
    }
}