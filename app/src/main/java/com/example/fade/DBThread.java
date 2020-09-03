package com.example.fade;

import android.content.Context;

import com.example.fade.entity.Group;
import com.example.fade.entity.Person;
import com.example.fade.DAO.*;

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
    static class SelectGroupThraed extends Thread {
        ArrayList<Group> groupList;
        public SelectGroupThraed(ArrayList<Group> groupList) {
            this.groupList = groupList;
        }
        @Override
        public void run(){
            this.groupList.clear();
            this.groupList.addAll(groupDAO.getAll());
        }
    }
    static class InsertTGroupThraed extends Thread {
        Group group;
        public InsertTGroupThraed(Group group) {
            this.group=group;
        }
        @Override
        public void run(){
            groupDAO.insert(group);
        }
    }
    static class UpdateGroupThraed extends Thread {
        Group  group;
        public UpdateGroupThraed(Group group) {
            this.group = group;}
        @Override
        public void run(){
            groupDAO.update(this.group);
        }
    }
    static class DeleteGroupThraed extends Thread {
        Group  group;
        public DeleteGroupThraed(Group group) {
            this.group = group;}
        @Override
        public void run(){
            groupDAO.delete(this.group);
        }
    }

    //퍼슨Table 관리 스래드
    static class SelectPersonThraed extends Thread {
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
    static class InsertPersonThraed extends Thread {
        Person person;
        public InsertPersonThraed(Person person) {
            this.person=person;
        }
        @Override
        public void run(){
            personDAO.insert(person);
        }
    }
    static class DeletePersonThraed extends Thread {
        Person person;
        public DeletePersonThraed(Person person) {
            this.person = person;}
        @Override
        public void run(){
            personDAO.delete(this.person);
        }
    }

    //복합 스래드
    static class SelectPListByGidThread extends Thread {

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
    static class SelectPidListByGIdThraed extends Thread {
        GroupDAO dao;
        int  gid;
        ArrayList<Integer> personIdList;

        public SelectPidListByGIdThraed(GroupDAO dao, int gid, ArrayList<Integer> personIdList) {
            this.dao = dao;
            this.gid=gid;
            this.personIdList=personIdList;

        }

        @Override
        public void run(){
            personIdList.clear();
//            int count = dao.getCount();
//            Log.d("COUNT", ""+count);
            personIdList.addAll(Converters.fromString(dao.getPidList(gid)));
        }
    }

}
