package com.example.fade;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fade.DB.DBThread;
import com.example.fade.DB.entity.Group;
import com.example.fade.DB.entity.Person;
import com.example.fade.Server.CommServer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //AppDB를 static으로 생성할 때도 쓰임
    //다른 Actrivity or Fragment에서 메인의 그룹리싸이클러뷰를 새로고침 하기 위함
    public  static Context CONTEXT;

    DrawerLayout drawerLayout;

    Menu mMenu;

    RecyclerView rv;
    GroupAdapter groupAdapter;
    ArrayList<Group> groupList=new ArrayList<Group>();
    ArrayList<Uri> groupUriList;
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

        groupList.clear();
        //groupList에 DB불러오기
        DBThread.SelectGroupThraed t =new DBThread.SelectGroupThraed(groupList);
        t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        //어댑터 생성 후 리싸이클러뷰 어뎁터랑 연결
        groupAdapter = new GroupAdapter(groupList);
        rv.setAdapter(groupAdapter);

//        Animation fab_open = AnimationUtils.loadAnimation(CONTEXT, R.anim.fab_open);
//        Animation fab_close = AnimationUtils.loadAnimation(CONTEXT, R.anim.fab_close);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);

        fab.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenu = menu;
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
            case R.id.menu_galleryRefresh:
                GetPermission.verifyStoragePermissions(this);

                Toast.makeText(getApplicationContext(),"이미지 분류 시작", Toast.LENGTH_SHORT ).show();

                mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(false);

                Handler handler = new Handler();
                //selectGalleryImage selectGalleryImage = new selectGalleryImage(getApplicationContext());
                Thread t = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            //갤러리 이미지 가져오기
                            ArrayList<byte[]> byteList = getByteArrayOfRecentlyImages();
                            CommServer commServer = new CommServer(getApplicationContext());
                            Log.i("updateGalleryImg","실행 시작");
                            //서버에 보낸 후 값 받기
                            commServer.updateGalleryImg(byteList, groupUriList);//갤러리 경로변경할 이미지의 uri 리스트 따로 받아옴
                            handler.post(() -> {
                                mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(true);
                                Toast.makeText(getApplicationContext(),"이미지 분류 완료", Toast.LENGTH_SHORT ).show();
                            });

                        }catch (IOException e){
                            Log.i("updateGalleryImg ", e.getMessage());
                            mMenu.findItem(R.id.menu_galleryRefresh).setEnabled(true);
                            handler.post(() -> Toast.makeText(getApplicationContext(),"이미지 분류 실패", Toast.LENGTH_SHORT ).show());
                        }
                    }
                };
             t.start();

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
//        groupAdapter.notifyDataSetChanged();
        rv.removeAllViewsInLayout();
        //그룹리스트 새로고침
        groupList.clear();
        new DBThread.SelectGroupThraed(groupList).start();
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
                DBThread.InsertTGroupThraed t1 = new DBThread.InsertTGroupThraed(group);
                DBThread.SelectGroupThraed t2 = new DBThread.SelectGroupThraed(groupList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                groupAdapter.notifyDataSetChanged();
                rv.scrollToPosition(groupList.size()-1);

                Toast.makeText(getApplicationContext(),"그룹 등록 중입니다", Toast.LENGTH_SHORT).show();

                //방금 만든 Group의 GID 얻는 부분
                int[] gid = new int[1];
                DBThread.SelectRecentlyGIDThread t3 = new DBThread.SelectRecentlyGIDThread(gid);
                t3.start();
                try { t3.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                //서버에 그룹모델 만들도록 하는 코드
                new CommServer(getApplicationContext()).postRegisterGroup(LoginActivity.UserID, gid[0], result.personIDList);
                Toast.makeText(getApplicationContext(),"그룹 등록을 성공했습니다!", Toast.LENGTH_SHORT).show();

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
    private ArrayList<byte[]> getByteArrayOfRecentlyImages()   //최근 갤러리 이미지 가져오기
    {
        Uri uri;
        SimpleDateFormat dateFormat;
        ArrayList<byte[]> byteList = new ArrayList<byte[]>();
        groupUriList = new ArrayList<>();
        String last_update; //제일 마지막에 업뎃한 시간
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        last_update = "2020/09/16";

        ConvertFile convertFile  = new ConvertFile(getApplicationContext());


        String[] projection = {
//                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
        };

        String where = MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'";
        Cursor cursor = getContentResolver().query(uri, projection, where, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");


//        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA); //절대경로 메타데이터에서 가져오기
        int columnDisplayname = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);//파일 이름 메타데이터에서 가져오기
        int columnDate = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED); //생성된 날짜 메타데이터에서 가져오기
        int columnId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID); //생성된 날짜 메타데이터에서 가져오기

        int lastIndex;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        int i = 0;

        ArrayList<Bitmap>bitmaps = new ArrayList<Bitmap>();
        while (cursor.moveToNext())
        {

            Long IdOfImage = cursor.getLong(columnId);
            Uri  uriimage = Uri.withAppendedPath(uri,""+IdOfImage);


            String nameOfFile = cursor.getString(columnDisplayname);
            String DateOfImage = dateFormat.format(new Date(cursor.getLong(columnDate) * 1000L));
            ContentResolver contentResolver = getContentResolver();

            int compare_time_last = DateOfImage.compareTo(last_update);//사진이 생성된 날짜와 마지막 업뎃 날짜를 비교하여
            if(compare_time_last>=0){
                try{
                    bitmaps.add(convertFile.resize(uriimage, 200));
                    groupUriList.add(uriimage);
                    i++;

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        cursor.close();

        //리사이즈된 비트맵들을 바이트들로 바꿔줌
        ConvertFile.bitmapsToByteArrayThread t = convertFile.new bitmapsToByteArrayThread(bitmaps,byteList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        Log.i("getByteArrayOfRecentlyImages", i+"개의 사진의 바이트가 담긴 리스트 리턴함");
        return byteList;
    }
}

class  GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GVHolder>{

    ArrayList<Group> groupList;

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

    GroupAdapter(ArrayList<Group> groupList){
        this.groupList=groupList;
    }

    @NonNull
    @Override
    public GVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grouplist, parent, false);
        return new GVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GVHolder holder, final int position) {

        final EditText et_name = holder.view.findViewById(R.id.et_groupList_name);
        et_name.setText(groupList.get(position).getName());

        //연필 버튼 눌렀을 때 그룹이름을 수정하게 해주는 부분
        final ImageButton ibtn_edit = holder.view.findViewById(R.id.ibtn_editGroupName);
        final ImageButton ibtn_check = holder.view.findViewById(R.id.ibtn_editCheck);

        //연필버튼을 누르면 연필버튼을 없애고 체크버튼을 나타냄. 그리고 이름을 수정 가능하게 함
        ibtn_edit.setOnClickListener(view -> {
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

            Group group = groupList.get(position);
            group.setName(et_name.getText().toString());
            new DBThread.UpdateGroupThraed(group).start();

            et_name.setEnabled(false);
            ibtn_check.setVisibility(View.GONE);
            ibtn_edit.setVisibility(View.VISIBLE);
        });

        //그룹X버튼을 눌렀을 때 동작 (그룹을 삭제함)
        ImageButton ibtn_subGroup = holder.view.findViewById(R.id.ibtn_subGroup);
        ibtn_subGroup.setOnClickListener(new View.OnClickListener() {
            Group group = groupList.get(position);
            @Override
            public void onClick(View view) {
                DBThread.DeleteGroupThraed t1 = new DBThread.DeleteGroupThraed(groupList.get((position)));
                //꼭 삭제하고 리스트뷰 갱신을 위해 groupList를 바뀐 DB로 재갱신 해줘야함!
                DBThread.SelectGroupThraed t2 = new DBThread.SelectGroupThraed(groupList);
                t1.start();
                try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                t2.start();
                try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                notifyDataSetChanged();

                new CommServer(holder.view.getContext()).DeleteGroup(LoginActivity.UserID, group.getGid());
                Toast.makeText(holder.view.getContext(),"그룹 삭제를 성공했습니다!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton ibtn_editGroup = holder.view.findViewById(R.id.ibtn_editGroupPerson);
        ibtn_editGroup.setOnClickListener(view -> {

            //다이얼로그에서 받아올 값을 생성자로 넘겨줌(사용자가 편집한 pidList)
            ArrayList<Integer> result = new ArrayList<>();

            Group group = groupList.get(position);
            ArrayList<Integer> pidList = groupList.get(position).getPersonIDList();
            //현재 pid리스트와, 바뀐 pidList를 저장할 result를 인자로 넘김
            final EditGroupDialog editGroupDialog= new EditGroupDialog(view.getContext(), pidList, result, new CustomDialogClickListener() {
                @Override
                public void onPositiveClick() {
                    group.setPersonIDList(result);
                    DBThread.UpdateGroupThraed t1 = new DBThread.UpdateGroupThraed(group);
                    DBThread.SelectGroupThraed t2 = new DBThread.SelectGroupThraed(groupList);
                    t1.start();
                    try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                    t2.start();
                    try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                    notifyDataSetChanged();

//                    //서버에 그룹모델 수정하도록 하는 코드
                    new CommServer(holder.view.getContext()).postEditGroup(LoginActivity.UserID, group.getGid(), result);
                    Toast.makeText(holder.view.getContext(),"그룹 편집을 성공했습니다!", Toast.LENGTH_SHORT).show();
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
        ArrayList<Person> profileList=new ArrayList<Person>();

        //리싸이클러뷰 만들고 설정
        final RecyclerView rv = holder.view.findViewById(R.id.rv_group_profile);
        //수평으로 되게
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(linearLayoutManager);
        //rv.addItemDecoration(new DividerItemDecoration(view.getContext(),1));

        //현재 그룹이 가지고 있는 person리스트를 gid로 가져오기
        DBThread.SelectPListByGidThread t1 = new DBThread.SelectPListByGidThread(groupList.get(position).getGid(), profileList);
        t1.start();
        try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }

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

    ArrayList<Person> itemList;
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

    ProfileAdapter(ArrayList<Person> itemList){
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
        if(itemList.get(position).getProfile_picture() != null){
            Bitmap bitmap = convertFile.byteArrayToBitmap(itemList.get(position).getProfile_picture());
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

