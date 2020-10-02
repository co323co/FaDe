package com.example.fade.DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.fade.DB.DAO.GroupDAO;
import com.example.fade.DB.DAO.PersonDAO;
import com.example.fade.DB.entity.Group;
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
    static GroupDAO groupDAO = AppDatabase.getInstance(context).groupDAO();

    //UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!


    //그룹Table 관리 스래드

    public static class SelectGnameThraed extends Thread {
        int gidList;
        ArrayList<String> gnameList;
        public SelectGnameThraed(int gidList, ArrayList<String> gnameList) {
            this.gidList = gidList;
            this.gnameList = gnameList;

        }
        @Override
        public void run(){
            groupDAO.getGnameList(gidList);
//            this.gnameList.clear();
            this.gnameList.addAll(groupDAO.getGnameList(gidList));
        }
    }

    public static class SelectGidListByPid extends Thread {
        String pid;
        ArrayList<Integer> gidList;
        public SelectGidListByPid(int pid, ArrayList<Integer> gidList) {
            this.pid = Integer.toString(pid);
            this.gidList=gidList;
        }
        @Override
        public void run() {
            this.gidList.clear();
            this.gidList.addAll(groupDAO.getGidListByPid(pid));
        }
    }


    public static class InsertTGroupThraed extends Thread {
        Group group;
        public InsertTGroupThraed(Group group) {
            this.group=group;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run(){
            groupDAO.insert(group);
        }
    }

    public static class UpdateGroupThraed extends Thread {
        Group  group;
        public UpdateGroupThraed(Group group) {
            this.group = group;}
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run(){
            groupDAO.update(this.group);
        }
    }
    public static class DeleteGroupThraed extends Thread {
        Group  group;
        public DeleteGroupThraed(Group group) {
            this.group = group;}
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run(){
            groupDAO.delete(this.group);
        }
    }

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

    //복합 스래드
    public static class SelectPListByGidThread extends Thread {

        ArrayList<Person> personList;
        int gid;

        public SelectPListByGidThread(int gid, ArrayList<Person> personList) {
            this.personList = personList;
            this.gid=gid;
        }

        @Override
        public void run(){
            ArrayList<Integer> personIdList  = Converters.fromString(groupDAO.getPidList(gid));
            this.personList.clear();
            this.personList.addAll(personDAO.getByIdList(personIdList));
        }
    }
    public static class SelectPidListByGIdThraed extends Thread {
        int  gid;
        ArrayList<Integer> personIdList;

        public SelectPidListByGIdThraed(int gid, ArrayList<Integer> personIdList) {
            this.gid=gid;
            this.personIdList=personIdList;

        }

        @Override
        public void run(){
            personIdList.clear();
//            int count = dao.getCount();
//            Log.d("COUNT", ""+count);
            personIdList.addAll(Converters.fromString(groupDAO.getPidList(gid)));
        }
    }

    public static class selectgid extends Thread {
        ArrayList<Integer> gidList;
        public selectgid(ArrayList<Integer> gidList) {
            this.gidList = gidList;
        }
        @Override
        public void run(){
            this.gidList.clear();
            this.gidList.addAll(groupDAO.getGidList());
        }
    }

}
