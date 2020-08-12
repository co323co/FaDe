package com.example.fade;

import com.example.fade.entity.Person;

import java.util.ArrayList;

public class DBThread {

    static class SelectPersonByIdListThraed extends Thread {
        PersonDAO dao;
//        ArrayList<Integer> personIdList;
        ArrayList<Person> personList;
        int gid;

        public SelectPersonByIdListThraed(PersonDAO dao, int gid, ArrayList<Person> personList) {
            this.dao = dao;
//            this.personIdList=personIdList;
            this.personList = personList;
            this.gid=gid;
        }

        @Override
        public void run(){
            this.personList.clear();
            this.personList.addAll(dao.getByIdList(gid));
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
            this.personIdList.clear();
            personIdList.addAll(dao.getPersonIDList(gid));
        }
    }

}
