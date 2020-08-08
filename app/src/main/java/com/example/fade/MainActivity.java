package com.example.fade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;

    GroupAdapter groupAdapter;
    GroupDAO dao;
    ArrayList<Group> groupList=new ArrayList<Group>();

    int n=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //서랍에 연결해주면, 원래 배경은 어차피 서랍에서 백(activity_main.xml)으로 인클루드 해줌
        setContentView(R.layout.drawer_main);

        //툴바 설정
        Toolbar tb = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar() ;
        ab.setDisplayShowTitleEnabled(false);
        //이걸 해줘야 사용자가 버튼을 누를 수 있음
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //메뉴 버튼 설정
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawerLayout_main);

        //그룹을 보여줄 리스트뷰 만들기
        final RecyclerView rv = findViewById(R.id.rv_groupList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(linearLayoutManager);

        //db 만들기
        GroupDatabase db =GroupDatabase.getInstance(getApplicationContext());
        dao=db.groupDAO();
        //groupList에 DB불러오기
        new SelectGroupThraed(dao, groupList).start();
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        groupAdapter = new GroupAdapter(dao, groupList);
        rv.setAdapter(groupAdapter);

        Button addButton = findViewById(R.id.btn_addGroup);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Group group = new Group("테스트"+n);
                n++;
                InsertTGroupThraed t1 = new InsertTGroupThraed(dao, group);
                //꼭 삽입하고 리스트뷰 갱신을 위해 personList를 바뀐 DB로 재갱신 해줘야함!
                SelectGroupThraed t2 = new SelectGroupThraed(dao, groupList);
                t1.start();
                //join은 스레드가 끝날 때까지 기다려 줌
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                groupAdapter.notifyDataSetChanged();
                //포커스를 맨 아래로 맞춰줌
                rv.scrollToPosition(groupList.size()-1);
            }
        });

    }

    //메뉴 버튼을 눌렀을 때 드로우가 열리도록 해줌
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if(item_id==android.R.id.home) {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //다른 아이템이 클릭되었을 시 닫아줌
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}


//UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!
class InsertTGroupThraed extends Thread {
    GroupDAO dao;
    Group group;
    public InsertTGroupThraed(GroupDAO dao, Group group) {
        this.dao = dao;
        this.group=group;
    }
    @Override
    public void run(){
        dao.insert(group);
    }
}
//UI문제때문에 DAO는 메인스레드에서 쓸 수 없음, 백그라운드 스레드에서 실행해야 함!
//인자인 personList를 갱신해줌
class SelectGroupThraed extends Thread {
    GroupDAO dao;
    ArrayList<Group> groupList;
    public SelectGroupThraed(GroupDAO dao, ArrayList<Group> groupList) {
        this.dao = dao;
        this.groupList = groupList;
    }
    @Override
    public void run(){
        this.groupList.clear();
        this.groupList.addAll(dao.getAll());
    }
}
class DeleteGroupThraed extends Thread {
    GroupDAO dao;
    Group  group;
    public DeleteGroupThraed(GroupDAO dao, Group group) {
        this.dao=dao;
        this.group = group;}
    @Override
    public void run(){
        dao.delete(this.group);
    }
}

class UpdateGroupThraed extends Thread {
    GroupDAO dao;
    Group  group;
    public UpdateGroupThraed(GroupDAO dao, Group group) {
        this.dao=dao;
        this.group = group;}
    @Override
    public void run(){
        dao.update(this.group);
    }
}

class  GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GVHolder>{

    ArrayList<Group> groupList;
    GroupDAO dao;

    class GVHolder extends RecyclerView.ViewHolder{
        public View view;
        boolean isExpanded = false;

        public GVHolder(@NonNull final View itemView) {
            super(itemView);
            view=itemView;

            //아이템뷰의 클릭이벤트
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleLayout(isExpanded,view,(LinearLayout)view.findViewById(R.id.layoutExpand));
                    if  (isExpanded==false) isExpanded=true;
                    else isExpanded=false;
                }
            });
        }
    }

    GroupAdapter(GroupDAO dao, ArrayList<Group> groupList){
        this.dao=dao;
        this.groupList=groupList;
    }

    @NonNull
    @Override
    public GVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grouplist, parent, false);
        return new GroupAdapter.GVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GVHolder holder, final int position) {

        final EditText et_name = holder.view.findViewById(R.id.et_groupList_name);
        et_name.setText(groupList.get(position).getName());

        //연필 버튼 눌렀을 때 그룹이름을 수정하게 해주는 부분
        final ImageButton ibtn_edit = holder.view.findViewById(R.id.ibtn_editGroupName);
        final ImageButton ibtn_check = holder.view.findViewById(R.id.ibtn_editCheck);
        //연필버튼을 누르면 연필버튼을 없애고 체크버튼을 나타냄. 그리고 이름을 수정 가능하게 함
        ibtn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_name.setEnabled(true);
                //포커스 줌
                et_name.requestFocus();
                et_name.setSelection(et_name.length());
                ibtn_check.setVisibility(View.VISIBLE);
                ibtn_edit.setVisibility(View.GONE);

                //키보드 올리는 코드
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);



            }
        });
        //체크버튼을 누르면 이름 수정이 완료되고 DB에 반영됨. 체크버튼이 사라지고 다시 연필버튼이 나타남
        ibtn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Group group = groupList.get(position);
                group.setName(et_name.getText().toString());
                new UpdateGroupThraed(dao,group).start();

                et_name.setEnabled(false);
                ibtn_check.setVisibility(View.GONE);
                ibtn_edit.setVisibility(View.VISIBLE);
            }
        });

        //그룹X버튼을 눌렀을 때 동작 (그룹을 삭제함)
        Button btn_subGroup = holder.view.findViewById(R.id.btn_subGroup);
        btn_subGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteGroupThraed t1 = new DeleteGroupThraed(dao, groupList.get((position)));
                //꼭 삭제하고 리스트뷰 갱신을 위해 groupList를 바뀐 DB로 재갱신 해줘야함!
                SelectGroupThraed t2 = new SelectGroupThraed(dao, groupList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    private boolean toggleLayout(boolean isExpanded, View v, LinearLayout layoutExpand) {
        if (isExpanded) {
            Animations.expand(layoutExpand);
        } else {
            Animations.collapse(layoutExpand);
        }
        return isExpanded;

    }
}