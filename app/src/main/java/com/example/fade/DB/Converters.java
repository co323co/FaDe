package com.example.fade.DB;


import androidx.room.TypeConverter;

import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<Integer> fromString(String string) {
        if(string == null) return new ArrayList();
//        if(string.equals("")) return new ArrayList();
        String str[] = string.split(",");
        ArrayList<Integer> list = new ArrayList();
        //문자열이 " ,1," 이런 식으로 넘어옴. 그렇기 때문에 ,로 split한 결과의 맨 앞에는 공백이 들어가있음. 그래서 1부터 시작해야함.
        for (int i=1; i<str.length; i++) list.add(Integer.parseInt(str[i]));
        return list;
    }

    @TypeConverter
    public static String fromList(ArrayList<Integer> list) {
        String str=",";
        for(int i=0; i<list.size();i++) str+=(list.get(i)).toString()+",";
        return str;
    }
}
