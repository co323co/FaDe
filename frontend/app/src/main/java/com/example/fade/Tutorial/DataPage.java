package com.example.fade.Tutorial;

public class DataPage {
    int image;
    String title;
    int num;
    public DataPage(int image, String title, int num){
        this.image = image;
        this.title = title;
        this.num = num;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
