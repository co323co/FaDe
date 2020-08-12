package com.example.fade;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomDialog { }
interface CustomDialogClickListener {
    void onPositiveClick();
    void onNegativeClick();
}

class AddGroupDialog extends Dialog {

    Context context;
    private Button mPositiveButton;
    private Button mNegativeButton;

    private CustomDialogClickListener customDialogClickListener;

    //생성자 생성
    public AddGroupDialog(@NonNull Context context, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_group);

        //셋팅
        mPositiveButton=(Button)findViewById(R.id.btn_addGroup_ok);
        mNegativeButton=(Button)findViewById(R.id.btn_addGroup_no);

        //호출하는 곳에서 인터페이스 설계해줌으로써 사용
        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogClickListener.onPositiveClick();
                dismiss();
            }
        });
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogClickListener.onNegativeClick();
                dismiss();
            }
        });

        final RecyclerView rv_right = findViewById(R.id.rv_addGroupPerson_right);
        ListView lv_left = findViewById(R.id.lv_addGroupPerson_left);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext());
        rv_right.setLayoutManager(linearLayoutManager2);

        final ArrayList<Person> checkedList = new ArrayList<Person>();

        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        final RightAdapter rightAdapter = new RightAdapter(checkedList);
        rv_right.setAdapter(rightAdapter);

        //아이템클릭리스너
        final int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");
        final int DEFAULT_COLOR = Color.WHITE;

        final ArrayList<Person> personList=new ArrayList<Person>();

        PersonDatabase db =PersonDatabase.getInstance(getContext());
        PersonDAO dao =db.personDAO();
        SelectPersonThraed t  = new SelectPersonThraed(dao, personList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        final boolean isChecked[] = new boolean[personList.size()];

        LeftListAdapter leftListAdapter = new LeftListAdapter(checkedList, rightAdapter, isChecked);
        lv_left.setAdapter(leftListAdapter);

        //눌렀을 때 배경색 바뀌고 오른쪽으로 넘어가게끔
        lv_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (isChecked[i]==false){
                    view.setBackgroundColor(SELECTED_COLOR);
                    isChecked[i] = true;
                    checkedList.add(personList.get(i));
                    rightAdapter.notifyDataSetChanged();

                }
                else {
                    view.setBackgroundColor(DEFAULT_COLOR);
                    isChecked[i] = false;
                    checkedList.remove(personList.get(i));
                    rightAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    class LeftListAdapter extends BaseAdapter{

        LayoutInflater mLayoutInflater = null;

        ArrayList<Person> personList=new ArrayList<Person>();
        ArrayList<Person> checkedList;
        boolean isChecked[];
        RightAdapter rightAdapter;

        int SELECTED_COLOR = Color.parseColor("#C3E8E4E4");
        int DEFAULT_COLOR = Color.WHITE;

        LeftListAdapter(ArrayList<Person> checkedList, RightAdapter rightAdapter, boolean isChecked[]){

            mLayoutInflater = LayoutInflater.from((context));

            this.checkedList=checkedList;
            this.rightAdapter=rightAdapter;
            this.isChecked = isChecked;

            //personList에 DB에서 값 불러오기
            PersonDatabase db =PersonDatabase.getInstance(getContext());
            PersonDAO dao =db.personDAO();
            SelectPersonThraed t  = new SelectPersonThraed(dao, personList);
            t.start();
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        }

        @Override
        public int getCount() {
            return personList.size();
        }

        @Override
        public Object getItem(int i) {
            return personList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = mLayoutInflater.inflate(R.layout.item_profile_horizon, null);

            TextView name = view.findViewById(R.id.tv_profile_horizon_name);
            name.setText(personList.get(i).getName());

            if (isChecked[i]==true) view.setBackgroundColor(SELECTED_COLOR);


//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (view.isSelected()==false){
//                        view.setBackgroundColor(SELECTED_COLOR);
//                        view.setSelected(true);
//                        checkedList.add(personList.get(i));
//                    }
//                    else {
//                        view.setBackgroundColor(DEFAULT_COLOR);
//                        view.setSelected(false);
//                        checkedList.remove(personList.get(i));
//                    }
//                }
//            });
            return view;

        }
    }

    class RightAdapter extends RecyclerView.Adapter<RightAdapter.RVHolder>{

        ArrayList<Person> checkedList;

        public class RVHolder extends RecyclerView.ViewHolder {
            View view;
            public RVHolder(@NonNull final View itemView) {
                super(itemView);
                this.view=itemView;
            }
        }
        //어뎁터 생성자
        RightAdapter(ArrayList<Person> checkedList) {
            this.checkedList=checkedList;
        }
        @NonNull
        @Override
        public RVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_horizon, parent, false);
            return new RightAdapter.RVHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RVHolder holder, final int position) {
            TextView tv_name = holder.view.findViewById(R.id.tv_profile_horizon_name);
            tv_name.setText(checkedList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return checkedList.size();
        }

    }
}

