package com.example.fade.DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.fade.DB.DAO.PersonDAO;
import com.example.fade.DB.entity.Person;
import com.example.fade.MainActivity;

import java.util.ArrayList;

public class DBThread {

//    private static DBThread INSTANCE;
//
//    public static DBThread getInstance() {
//        if(INSTANCE==null) INSTANCE=new DBThread();
//        return INSTANCE;
//    }

    static Context context = MainActivity.CONTEXT;
    static PersonDAO personDAO = AppDatabase.getInstance(context).personDAO();

    //UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!


    //그룹Table 관리 스래드

    //퍼슨Table 관리 스래드
    public static class SelectPersonThraed extends Thread {
        ArrayList<Person> personList;
        public SelectPersonThraed(ArrayList<Person> personList) {
            this.personList = personList;
        }
        @Override
        public void run(){
            this.personList.clear();
            this.personList.addAll(personDAO.getAll());
        }
    }

    //retrun이 없는 스래드 특성상 인자로 int값을 call by ref 하기 위한 배열임. 첫번째 값인 0만 사용해야 함
    public static class SelectRecentlyPIDThread extends Thread {
        int[] pid;
        public SelectRecentlyPIDThread(int[] pid) {this.pid=pid;}
        @Override
        public void run() {
            this.pid[0]=personDAO.getRecentlyPID();
        }
    }

    public static class InsertPersonThraed extends Thread {
        Person person;
        public InsertPersonThraed(Person person) {
            this.person=person;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run(){
            personDAO.insert(person);
        }
    }
    public static class DeletePersonThraed extends Thread {
        Person person;
        public DeletePersonThraed(Person person) {
            this.person = person;}
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run(){
            personDAO.delete(this.person);
        }
    }

}
