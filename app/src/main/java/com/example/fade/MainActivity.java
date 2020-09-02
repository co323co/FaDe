package com.example.fade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fade.entity.Group;
import com.example.fade.entity.Person;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getSystemService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //다른 Actrivity or Fragment에서 메인의 그룹리싸이클러뷰를 새로고침 하기 위함
    public  static Context CONTEXT;

    DrawerLayout drawerLayout;

    RecyclerView rv;
    GroupAdapter groupAdapter;
    GroupDAO dao;
    ArrayList<Group> groupList=new ArrayList<Group>();

    int n=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTEXT=this;

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
        rv = findViewById(R.id.rv_groupList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(linearLayoutManager);

        //db 만들기
        AppDatabase db =AppDatabase.getInstance(getApplicationContext());
        dao=db.groupDAO();
        //groupList에 DB불러오기
        new SelectGroupThraed(dao, groupList).start();
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        groupAdapter = new GroupAdapter(dao, groupList);
        rv.setAdapter(groupAdapter);

//        Animation fab_open = AnimationUtils.loadAnimation(CONTEXT, R.anim.fab_open);
//        Animation fab_close = AnimationUtils.loadAnimation(CONTEXT, R.anim.fab_close);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);

        fab.setOnClickListener(this);
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

    public void onResume(){
        super.onResume();
        rv.removeAllViewsInLayout();
        rv.setAdapter(groupAdapter);
    }

    //다이얼로그에서 받아올 값들을 클래스로 묶어둔 것
    class rst {
        String name;
        ArrayList<Integer> personIDList=new ArrayList<Integer>();
    }

    //플로팅 버튼
    @Override
    public void onClick(View view) {

        final rst result = new rst();
        //다이얼로그에서 받아올 값을 생성자로 넘겨줌
        final AddGroupDialog addGroupDialog= new AddGroupDialog(this, result, new CustomDialogClickListener() {
            @Override
            public void onPositiveClick() {
                Group group = new Group(result.name, result.personIDList);
                InsertTGroupThraed t1 = new InsertTGroupThraed(dao, group);
                SelectGroupThraed t2 = new SelectGroupThraed(dao, groupList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                groupAdapter.notifyDataSetChanged();
                rv.scrollToPosition(groupList.size()-1);
            }
            @Override
            public void onNegativeClick() { }
        });
        //다이얼로그 밖을 터치했을 때 다이얼로그가 꺼짐
        addGroupDialog.setCanceledOnTouchOutside(true);
        //뒤로가기 버튼으로 다이얼로그 끌 수 있음
        addGroupDialog.setCancelable(true);
        //레이아웃
        addGroupDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        addGroupDialog.show();
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
            Button btn_view = (Button)view.findViewById(R.id.btn_groupView);
            //아이템뷰의 클릭이벤트
            btn_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if  (isExpanded==false) isExpanded=true;
                    else isExpanded=false;
                    toggleLayout(isExpanded,itemView,(LinearLayout)itemView.findViewById(R.id.layoutExpand));
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
        ImageButton ibtn_subGroup = holder.view.findViewById(R.id.ibtn_subGroup);
        ibtn_subGroup.setOnClickListener(new View.OnClickListener() {
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


        //프로필리싸이클러뷰 생성 과정
        ProfileAdapter profileAdapter;
        PersonDAO personDao;
        ArrayList<Integer> profileIdList=new ArrayList<Integer>();
        ArrayList<Person> profileList=new ArrayList<Person>();

        //리싸이클러뷰 만들고 설정
        final RecyclerView rv = holder.view.findViewById(R.id.rv_group_profile);
        //수평으로 되게
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(linearLayoutManager);
        //rv.addItemDecoration(new DividerItemDecoration(view.getContext(),1));

        //db 만들기
        AppDatabase db =AppDatabase.getInstance(holder.view.getContext());
        personDao=db.personDAO();


        //현재 그룹이 가지고 있는 pid리스트를 가져오기
        DBThread.SelectPidListByGIdThraed t1 = new DBThread.SelectPidListByGIdThraed(dao,groupList.get(position).getGid(), profileIdList);
        t1.start();
        try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        //받아온 pid리스트를 통해 person리스트를 얻어옴
//        DBThread.SelectPersonByIdListThraed t2 = new DBThread.SelectPersonByIdListThraed(personDao, groupList.get(position).getGid(), profileList);
//        t2.start();
//        try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }

//        Log.d("태그", "position: " + position + "값 : "+profileIdList.get(0));
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        profileAdapter = new ProfileAdapter(personDao, profileList);
        rv.setAdapter(profileAdapter);
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

class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.PVHolder>{

    ArrayList<Person> itemList=new ArrayList<Person>();
    PersonDAO dao;

    public class PVHolder extends RecyclerView.ViewHolder{
        public View view;
        public PVHolder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
        }
    }

    ProfileAdapter(PersonDAO dao, ArrayList<Person> itemList){
        this.dao=dao;
        this.itemList=itemList;
    }

    @NonNull
    @Override
    public PVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_profile, parent, false);
        return new PVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PVHolder holder, int position) {

        TextView tv_name = holder.view.findViewById(R.id.tv_group_profileName);
        tv_name.setText(itemList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


}

class SelectPersonThraed extends Thread {
    PersonDAO dao;
    ArrayList<Person> personList;
    public SelectPersonThraed(PersonDAO dao, ArrayList<Person> personList) {
        this.dao = dao;
        this.personList = personList;
    }
    @Override
    public void run(){
        this.personList.clear();
        this.personList.addAll(dao.getAll());
    }
}