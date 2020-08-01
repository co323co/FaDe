package com.example.fade;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainDrawerFragment extends Fragment {

    PersonAdapter personAdapter;
    PersonDAO dao;
    ArrayList<Person> personList=new ArrayList<Person>();
    int n=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_drawer_main,container,false);
        //리싸이클러뷰 어뎁터랑 연결
        RecyclerView rv = view.findViewById(R.id.rv_nameList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(view.getContext(),1));
        personAdapter = new PersonAdapter(personList);
        rv.setAdapter(personAdapter);

        AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "db").build();
        dao=db.personDAO();
        new SelectThraed(personList).start();
        personAdapter.notifyDataSetChanged();

        Button addButton = view.findViewById(R.id.btn_addPerson);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Person person = new Person("테스트"+n);
                n++;
                InsertThraed t1 = new InsertThraed(person);
                SelectThraed t2 = new SelectThraed(personList);
                t1.start();
                //join은 스레드가 끝날 때까지 기다려 줌
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                personAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    //UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!
    class InsertThraed extends Thread {
        Person person;
        public InsertThraed(Person person) {
            this.person=person;
        }
        @Override
        public void run(){
            dao.insert(person);
        }
    }
    //UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!
    //인자인 personList를 갱신해줌
    class SelectThraed extends Thread {
        ArrayList<Person> personList;
        public SelectThraed(ArrayList<Person> personList) {
            this.personList = personList;
        }
        @Override
        public void run(){
            this.personList.clear();
            this.personList.addAll(dao.getAll());
        }
    }
}



//PersonRecyclerViewAdapter
class  PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PVHolder>{

    ArrayList<Person> personList;
    PersonDAO dao;
    class PVHolder extends  RecyclerView.ViewHolder {

        public View view;
        public PVHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }
    }

    PersonAdapter(ArrayList<Person> personList){
        this.personList=personList;
    }
    @NonNull
    @Override
    public PVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_namelist, parent, false);
        return new PVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PVHolder holder, int position) {

        TextView tv_name = holder.view.findViewById(R.id.tv_nameList_name);
        tv_name.setText(personList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

}