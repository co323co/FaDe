package com.example.fade.DB;


import androidx.room.TypeConverter;

import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<Integer> fromString(String string) {
        if(string.equals("")) return new ArrayList();

        String str[] = string.split(",");
        ArrayList<Integer> list = new ArrayList();
        for (int i=0; i<str.length; i++) list.add(Integer.parseInt(str[i]));
        return list;
    }

    @TypeConverter
    public static String fromList(ArrayList<Integer> list) {
        String str="";
        for(int i=0; i<list.size();i++) str+=(list.get(i)).toString()+",";
        return str;
    }
}
