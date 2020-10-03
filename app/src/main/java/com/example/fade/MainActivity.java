package com.example.fade;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.fade.Alarm.AlarmService;
import com.example.fade.Alarm.UploadWorker;
import com.example.fade.Server.CommServer;
import com.example.fade.Server.GroupData;
import com.example.fade.Server.PersonData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //AppDB를 static으로 생성할 때도 쓰임
    //다른 Actrivity or Fragment에서 메인의 그룹리싸이클러뷰를 새로고침 하기 위함
    public  static Context CONTEXT;
    AlertDialog alertDialog;
    SimpleDateFormat dateFormat;
    DrawerLayout drawerLayout;

    public Menu mMenu;
    String last_update;
    RecyclerView rv;
    GroupAdapter groupAdapter;
    ArrayList<GroupData> groupList=new ArrayList<>();
    ArrayList<Uri> groupUriList;
    SharedPreferences sharedPrefs;

    int n=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTEXT=this;


        Log.d("server","onCreate");
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);

        fab.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenu = menu;
        MenuItem item = (MenuItem) menu.findItem(R.id.menu_alarm);
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        sharedPrefs = getSharedPreferences("alarm_check", MODE_PRIVATE);
        last_update = sharedPrefs.getString("last_update", dateFormat.format(new Date()));
//        last_update = "2020/09/30";
        Log.e("마지막 업뎃 날짜", last_update);
        item.setChecked(sharedPrefs.getBoolean("check_switch", false));
        if(item.isChecked()){
            startService(new Intent(this, AlarmService.class));
            final PeriodicWorkRequest saveRequest =
                    new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES)
                            .build();

            WorkManager
                    .getInstance(this)
                    //.enqueue(saveRequest);
                    //.enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.KEEP,saveRequest);
                    .enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.KEEP, saveRequest);

        }


        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id) {
            //메뉴 버튼을 눌렀을 때 드로우가 열리도록 해줌
            case android.R.id.home :
                if(drawerLayout.isDrawerOpen(GravityCompat.START))  drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.menu_logout:
                Toast.makeText(getApplicationContext(),"로그아웃",Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), LogoutActivity.class);
//                intent.putExtra("setting_name","로그아웃");
//                startActivity(intent);
                //로그아웃 액티비티  켜지고, 다시 로그인액티비티 켜니까 액티비티가 따당하고 켜져서 여기에 걍 코드올림
                GoogleSignInClient mGoogleSignInClient;
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.putExtra("재로그인", "true");
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.id.menu_tutorial:
                Toast.makeText(getApplicationContext(),"튜토리얼",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),TutorialActivity.class);
                startActivity(intent);

                break;

            case R.id.menu_alarm:

                if (item.isChecked()) {
                    AlertDialog.Builder builder_alarm = new AlertDialog.Builder(this);

                    builder_alarm.setTitle("갤러리 자동정리 기능 설정").setMessage("갤러리 자동정리 기능을 비활성화 하시겠습니까?");
                    builder_alarm.setPositiveButton("설정", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("gallery_update");
                            stopService(new Intent(getApplicationContext(), AlarmService.class));


                            item.setChecked(false);
                            Toast.makeText(getApplicationContext(),"갤러리 자동정리 비활성화",Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
                            editor.putBoolean("check_switch", item.isChecked());
                            editor.commit();

                        }
                    });
                    builder_alarm.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        { }
                    });
                    AlertDialog alertDialog_alarm = builder_alarm.create();
                    alertDialog_alarm.show();
                }
                else {
                    AlertDialog.Builder builder_alarm = new AlertDialog.Builder(this);

                    builder_alarm.setTitle("갤러리 자동정리 기능 설정").setMessage("갤러리 자동정리 기능을 사용하기 위해서 설정\n->검색 -> 배터리 사용량 최적화 -> 최적화하지 않은 앱 -> 전체 -> FaDe 설정해제 해야합니다.\n\n 갤러리 자동정리 기능을 활성화 하시겠습니까?");

                    builder_alarm.setPositiveButton("설정", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            startService(new Intent(CONTEXT, AlarmService.class));
                            final PeriodicWorkRequest saveRequest =
                                    new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES)
                                            .build();

                            WorkManager
                                    .getInstance(CONTEXT)
                                    //.enqueue(saveRequest);
                                    //.enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.KEEP,saveRequest);
                                    .enqueueUniquePeriodicWork("gallery_update", ExistingPeriodicWorkPolicy.REPLACE, saveRequest);

                            item.setChecked(true);
                            Toast.makeText(getApplicationContext(),"갤러리 자동정리 활성화",Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
                            editor.putBoolean("check_switch", item.isChecked());
                            editor.commit();
                        }
                    });
                    builder_alarm.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        { }
                    });
                    AlertDialog alertDialog_alarm = builder_alarm.create();
                    alertDialog_alarm.show();

                }

                break;
            case R.id.menu_option:
                Intent intent_option = new Intent(getApplicationContext(),OptionActivity.class);
                startActivity(intent_option);
                break;

            case R.id.menu_galleryRefresh:
                GetPermission.verifyStoragePermissions(this);

                Toast.makeText(getApplicationContext(),"이미지 분류 시작", Toast.LENGTH_SHORT ).show();

                mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(false);

                GalleryUpdate galleryUpdate = new GalleryUpdate(this, groupUriList, last_update);
                Handler handler = new Handler();
                //selectGalleryImage selectGalleryImage = new selectGalleryImage(getApplicationContext());
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            handler.post(() -> {
                                mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(true);
                                mMenu.findItem(R.id.menu_galleryRefresh).setActionView(new ProgressBar(CONTEXT));
                            });
                            //갤러리 이미지 가져오기
                            ArrayList<byte[]> byteList = galleryUpdate.getByteArrayOfRecentlyImages();
                            CommServer commServer = new CommServer(getApplicationContext());
                            Log.i("updateGalleryImg","실행 시작");
                            //서버에 보낸 후 값 받기
                            commServer.updateGalleryImg(byteList, galleryUpdate.groupUriList);//갤러리 경로변경할 이미지의 uri 리스트 따로 받아옴
                        }catch (IOException e){
                            Log.i("updateGalleryImg ", e.getMessage());
                            mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(true);
                            mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                        }
                    }
                };
             t.start();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume(){
        super.onResume();
        /////////rv 어뎁터 연결 코드
        //그룹리스트 새로고침

        groupList.clear();
        rv.removeAllViewsInLayout();

        Handler handler = new Handler();
        new Thread(){
            @Override
            public void run() {
                groupList.addAll(new CommServer(getApplicationContext()).getAllGroups());
                handler.post(() -> {
                    //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
                    groupAdapter = new GroupAdapter(groupList);
                    rv.setAdapter(groupAdapter);
                });
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //다른 아이템이 클릭되었을 시 닫아줌
        drawerLayout.closeDrawer(GravityCompat.START);
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
                if(result.name.length()==0)
                {
                    Toast.makeText(getApplicationContext(), "그룹 이름을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                //인물 0명 선택했을 경우 예외처리
                if(result.personIDList.size()==0)
                {
                    Toast.makeText(getApplicationContext(), "1명 이상 선택해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                CommServer commServer = new CommServer(getApplicationContext());
                Handler mHandler = new Handler(Looper.getMainLooper());
                new Thread(){
                    @Override
                    public void run() {
                        mHandler.postAtFrontOfQueue((Runnable) () -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            LayoutInflater inflater = (LayoutInflater)view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                            View view1 = inflater.inflate(R.layout.progress_alert_layout, null);
                            builder.setView(view1)
                                    .setCancelable(false);
                            TextView text = (TextView)view1.findViewById(R.id.alert_text);
                            text.setText("그룹을 등록 중 입니다\n\n잠시만 기다려주세요...");
                            alertDialog = builder.create();
                            alertDialog.show();
                        });
                        commServer.registerGroup(LoginActivity.UserEmail, result.name, result.personIDList);
                        groupList.clear();
                        groupList.addAll(commServer.getAllGroups());
                        mHandler.post(() -> {
                            groupAdapter.notifyDataSetChanged();
                            rv.scrollToPosition(groupList.size()-1);
                            Toast.makeText(getApplicationContext(),"그룹 등록을 성공했습니다!", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        });
                    }
                }.start();

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

 class  GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GVHolder>{
    AlertDialog alertDialog;
    ArrayList<GroupData> groupList;
    ArrayList<GroupData> favorites=new ArrayList<>();
    CommServer commServer;
    class GVHolder extends RecyclerView.ViewHolder{
        public View view;
        boolean isExpanded = false;

        public GVHolder(@NonNull final View itemView) {
            super(itemView);
            view=itemView;
            commServer=new CommServer(itemView.getContext());
            Button btn_view = (Button)view.findViewById(R.id.btn_groupView);
            //아이템뷰의 클릭이벤트
            btn_view.setOnClickListener((View.OnClickListener) view -> {
                if  (isExpanded==false) isExpanded=true;
                else isExpanded=false;
                toggleLayout(isExpanded,itemView,(LinearLayout)itemView.findViewById(R.id.layoutExpand));
            });
        }

    }

     GroupAdapter(ArrayList<GroupData> groupList){
        this.groupList=groupList;
        //즐겨찾기 추가된 그룹들(favorites가 1이상)은 favorites 리스트에 담아서 관리함
        for(GroupData group : this.groupList) {
            if(group.getFavorites()>0) favorites.add(group);
            if(favorites.size()==3) break; }
    }

    @NonNull
    @Override
    public GVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grouplist, parent, false);
        return new GVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GVHolder holder, final int position) {

        GroupData group = groupList.get(position);
        ImageButton ibtn_favorites = holder.view.findViewById(R.id.ibtn_Favorites);
        ImageView iv_star = holder.view.findViewById(R.id.iv_groupList_star);

        if(group.getFavorites()==0) {
            ibtn_favorites.setColorFilter(Color.parseColor("#7AB1C3"));
            iv_star.setVisibility(View.GONE);
        }
        else {
            ibtn_favorites.setColorFilter(Color.RED);
            iv_star.setVisibility(View.VISIBLE);
        }

        ibtn_favorites.setOnClickListener(view -> {
            Log.d("favorites test", group.getName());
            if(group.getFavorites()==0) {
                Log.d("favorites test",  "if f==0 / size : " + favorites.size());
                if(favorites.size()>=3){
                    Toast.makeText(view.getContext(), "즐겨찾기는 3개까지 가능합니다",Toast.LENGTH_SHORT).show();
                    return;
                }
                group.setFavorites(favorites.size()+1);
                favorites.add(group);
                Log.d("favorites test",  "size : " + favorites.size());
            }
            else {
                Log.d("favorites test",  "size : " + favorites.size());
                Boolean b = favorites.remove(group);
                Log.d("favorites test",  "size : " + favorites.size());
                Log.d("favorites test",  "isSucess : " + b.toString());

                group.setFavorites(0);
                Collections.sort(favorites, (g1, g2) -> {
                    if(g1.getFavorites()<g2.getFavorites())
                        return 1;
                    else if(g1.getFavorites()>g2.getFavorites())
                        return  -1;
                    return 0;
                });
                for(int i=0; i<favorites.size(); i++) { favorites.get(i).setFavorites(i+1);}

                Thread t = new Thread(){
                    @Override public void run() {
                        for(GroupData g : favorites)
                        { commServer.editGroup(g.getId(), null, null, g.getFavorites()); } }
                };t.start(); //try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
            }
            Handler handler = new Handler();
            Thread t = new Thread(){ @Override public void run() {
                commServer.editGroup(group.getId(), null,null,group.getFavorites());
                groupList.clear();
                groupList.addAll(commServer.getAllGroups());
                handler.post(() -> { notifyDataSetChanged(); });
            }};
            t.start(); //try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
            // ((MainActivity)MainActivity.CONTEXT).onResume();
        });

        final EditText et_name = holder.view.findViewById(R.id.et_groupList_name);
        final ImageButton ibtn_edit = holder.view.findViewById(R.id.ibtn_editGroupName);
        final ImageButton ibtn_check = holder.view.findViewById(R.id.ibtn_editCheck);
        final ImageButton ibtn_gallery = holder.view.findViewById(R.id.ibtn_openGroupGallery);

        et_name.setText(groupList.get(position).getName());

        //갤러리 버튼 누르면 intent로 갤러리 화면으로 들어가짐
        ibtn_gallery.setOnClickListener(view -> {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"image/*");
            view.getContext().startActivity(intent);
        });

        //연필버튼을 누르면 연필버튼을 없애고 체크버튼을 나타냄. 그리고 이름을 수정 가능하게 함
        ibtn_edit.setOnClickListener(view -> {
            Toast.makeText(holder.view.getContext(),"수정할 그룹 이름을 입력해주세요", Toast.LENGTH_SHORT).show();

            et_name.setEnabled(true);
            //포커스 줌
            et_name.requestFocus();
            et_name.setSelection(et_name.length());
            ibtn_check.setVisibility(View.VISIBLE);
            ibtn_edit.setVisibility(View.GONE);

            //키보드 올리는 코드
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        });
        //체크버튼을 누르면 이름 수정이 완료되고 DB에 반영됨. 체크버튼이 사라지고 다시 연필버튼이 나타남
        ibtn_check.setOnClickListener(view -> {

            group.setName(et_name.getText().toString());
            new Thread(){
                @Override
                public void run() {
                    commServer.editGroup(group.getId(), et_name.getText().toString(), null, null);
                }
            }.start();
            Toast.makeText(holder.view.getContext(),"그룹 이름 변경 완료", Toast.LENGTH_SHORT).show();
            et_name.setEnabled(false);
            ibtn_check.setVisibility(View.GONE);
            ibtn_edit.setVisibility(View.VISIBLE);
        });

        //그룹X버튼을 눌렀을 때 동작 (그룹을 삭제함)
        ImageButton ibtn_subGroup = holder.view.findViewById(R.id.ibtn_subGroup);
        ibtn_subGroup.setOnClickListener(new View.OnClickListener() {
            GroupData group = groupList.get(position);
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                new Thread(){
                    @Override
                    public void run() {
                        commServer.deleteGroup(LoginActivity.UserEmail, group.getId());
                        groupList = commServer.getAllGroups();
                        handler.post(() -> {
                            Toast.makeText(holder.view.getContext(),"그룹 삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        });
                    }
                }.start();
            }
        });

        ImageButton ibtn_editGroup = holder.view.findViewById(R.id.ibtn_editGroupPerson);
        ibtn_editGroup.setOnClickListener(view -> {

            //다이얼로그에서 받아올 값을 생성자로 넘겨줌(사용자가 편집한 pidList)
            ArrayList<Integer> result = new ArrayList<>();
            ArrayList<Integer> pidList = new ArrayList<>();
            Thread t = new Thread(){
                @Override
                public void run() {
                    pidList.addAll(commServer.getPidListByGid(group.getId()));
                }
            }; t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

            //현재 pid리스트와, 바뀐 pidList를 저장할 result를 인자로 넘김
            final EditGroupDialog editGroupDialog= new EditGroupDialog(view.getContext(), pidList, result, new CustomDialogClickListener() {
                @Override
                public void onPositiveClick() {
                    //인물 0명 선택했을 때 예외처리
                    if(result.size()==0)
                    {
                        Toast.makeText(view.getContext(), "1명 이상 선택해주세요",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Handler handler = new Handler();
                    new Thread(){
                        @Override
                        public void run() {
                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.postAtFrontOfQueue(new Runnable() {
                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                    LayoutInflater inflater = (LayoutInflater)view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View view1 = inflater.inflate(R.layout.progress_alert_layout, null);
                                    builder.setView(view1)
                                            .setCancelable(false);
                                    TextView text = (TextView)view1.findViewById(R.id.alert_text);
                                    text.setText("그룹을 편집 중 입니다\n\n잠시만 기다려주세요...");
                                    alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            });
                            commServer.editGroup(group.getId(), null, result, null);
                            groupList = commServer.getAllGroups();
                            handler.post(() -> {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.dismiss();
                                        Toast.makeText(holder.view.getContext(),"그룹 편집을 성공했습니다!", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                notifyDataSetChanged();
                            });

                        }
                    }.start();
                }
                @Override
                public void onNegativeClick() { }
            });
            //다이얼로그 밖을 터치했을 때 다이얼로그가 꺼짐
            editGroupDialog.setCanceledOnTouchOutside(true);
            //뒤로가기 버튼으로 다이얼로그 끌 수 있음
            editGroupDialog.setCancelable(true);
            //레이아웃
            editGroupDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            editGroupDialog.show();

        });

        //////////////////////////////////
        //프로필리싸이클러뷰 생성 과정
        /////////////////////////////////
        ProfileAdapter profileAdapter;
        ArrayList<PersonData> profileList=new ArrayList<>();
        Thread t = new Thread(){
            @Override
            public void run() {
                profileList.addAll(commServer.getPersonsByGid(group.getId()));
            }
        }; t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        //리싸이클러뷰 만들고 설정
        final RecyclerView rv = holder.view.findViewById(R.id.rv_group_profile);
        //수평으로 되게
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(linearLayoutManager);

        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        profileAdapter = new ProfileAdapter(profileList);
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

    ArrayList<PersonData> itemList;
    Context context;

    public ProfileAdapter(Context context){
        this.context = context;
    }

    public class PVHolder extends RecyclerView.ViewHolder{
        public View view;
        public PVHolder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
        }
    }

    ProfileAdapter(ArrayList<PersonData> itemList){
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

        ImageView iv_profile = holder.view.findViewById(R.id.iv_group_profileImgae);
        iv_profile.setBackground(holder.view.getContext().getDrawable(R.drawable.background_rounding));
        iv_profile.setClipToOutline(true);

        ConvertFile convertFile = new ConvertFile(context);

        //프로필 사진 없으면 기본 이미지 띄움
        if(itemList.get(position).getThumbnail() != null){
            //Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)
            Bitmap bitmap = convertFile.byteArrayToBitmap(itemList.get(position).getThumbnail());
            iv_profile.setImageBitmap(bitmap);
        }
        else
        { iv_profile.setImageResource(R.drawable.ic_profile); }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

