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

    //AppDB??? static?????? ????????? ?????? ??????
    //?????? Actrivity or Fragment?????? ????????? ??????????????????????????? ???????????? ?????? ??????
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
        //????????? ???????????????, ?????? ????????? ????????? ???????????? ???(activity_main.xml)?????? ???????????? ??????
        setContentView(R.layout.drawer_main);

        //?????? ??????
        Toolbar tb = (Toolbar) findViewById(R.id.main_toolbar) ;
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar() ;
        ab.setDisplayShowTitleEnabled(false);
        //?????? ????????? ???????????? ????????? ?????? ??? ??????
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //?????? ?????? ??????
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawerLayout_main);

        //????????? ????????? ???????????? ?????????
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

        Thread t = new Thread(){
            @Override
            public void run() {
                CommServer commServer = new CommServer(getApplicationContext());
                last_update = commServer.getLastUpdate(LoginActivity.UserEmail);
            }
        }; t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        Log.e("????????? ?????? ??????", last_update);
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
            //?????? ????????? ????????? ??? ???????????? ???????????? ??????
            case android.R.id.home :
                if(drawerLayout.isDrawerOpen(GravityCompat.START))  drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.menu_logout:
                Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_SHORT).show();

                sharedPrefs = getSharedPreferences("alarm_check", MODE_PRIVATE);
                if(sharedPrefs.getBoolean("check_switch", false)){
                    WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("gallery_update");
                    stopService(new Intent(getApplicationContext(), AlarmService.class));
                    Toast.makeText(getApplicationContext(),"????????? ?????? ?????? ?????? ????????????",Toast.LENGTH_SHORT).show();

                }
                SharedPreferences.Editor editor = getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
                editor.putBoolean("check_switch", false);
                editor.commit();



//                Intent intent = new Intent(getApplicationContext(), LogoutActivity.class);
//                intent.putExtra("setting_name","????????????");
//                startActivity(intent);
                //???????????? ????????????  ?????????, ?????? ????????????????????? ?????? ??????????????? ??????
                GoogleSignInClient mGoogleSignInClient;
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.putExtra("????????????", "true");
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.id.menu_tutorial:
                Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),TutorialActivity.class);
                startActivity(intent);

                break;

            case R.id.menu_alarm:

                if (item.isChecked()) {
                    AlertDialog.Builder builder_alarm = new AlertDialog.Builder(this);

                    builder_alarm.setTitle("????????? ???????????? ?????? ??????").setMessage("????????? ???????????? ????????? ???????????? ???????????????????");
                    builder_alarm.setPositiveButton("??????", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("gallery_update");
                            stopService(new Intent(getApplicationContext(), AlarmService.class));


                            item.setChecked(false);
                            Toast.makeText(getApplicationContext(),"????????? ???????????? ????????????",Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
                            editor.putBoolean("check_switch", item.isChecked());
                            editor.commit();

                        }
                    });
                    builder_alarm.setNegativeButton("??????", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        { }
                    });
                    AlertDialog alertDialog_alarm = builder_alarm.create();
                    alertDialog_alarm.show();
                }
                else {
                    AlertDialog.Builder builder_alarm = new AlertDialog.Builder(this);

                    builder_alarm.setTitle("????????? ???????????? ?????? ??????").setMessage("????????? ???????????? ????????? ???????????? ????????? ??????\n->?????? -> ????????? ????????? ????????? -> ??????????????? ?????? ??? -> ?????? -> FaDe ???????????? ???????????????.\n\n ????????? ???????????? ????????? ????????? ???????????????????");

                    builder_alarm.setPositiveButton("??????", new DialogInterface.OnClickListener(){
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
                            Toast.makeText(getApplicationContext(),"????????? ???????????? ?????????",Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
                            editor.putBoolean("check_switch", item.isChecked());
                            editor.commit();
                        }
                    });
                    builder_alarm.setNegativeButton("??????", new DialogInterface.OnClickListener(){
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

                Toast.makeText(getApplicationContext(),"????????? ?????? ??????", Toast.LENGTH_SHORT ).show();

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
                            //????????? ????????? ????????????
                            ArrayList<byte[]> byteList = galleryUpdate.getByteArrayOfRecentlyImages();
                            CommServer commServer = new CommServer(getApplicationContext());
                            Log.i("updateGalleryImg","?????? ??????");
                            //????????? ?????? ??? ??? ??????
                            commServer.updateGalleryImg(byteList, galleryUpdate.groupUriList);//????????? ??????????????? ???????????? uri ????????? ?????? ?????????
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
        /////////rv ????????? ?????? ??????
        //??????????????? ????????????

        groupList.clear();
        rv.removeAllViewsInLayout();

        Handler handler = new Handler();
        new Thread(){
            @Override
            public void run() {
                groupList.addAll(new CommServer(getApplicationContext()).getAllGroups());
                handler.post(() -> {
                    //????????? ?????? ??? ?????????????????? ???????????? ??????
                    groupAdapter = new GroupAdapter(groupList);
                    rv.setAdapter(groupAdapter);
                });
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //?????? ???????????? ??????????????? ??? ?????????
        drawerLayout.closeDrawer(GravityCompat.START);
    }



    //????????????????????? ????????? ????????? ???????????? ????????? ???
    class rst {
        String name;
        ArrayList<Integer> personIDList=new ArrayList<Integer>();
    }
    //????????? ??????
    @Override
    public void onClick(View view) {

        final rst result = new rst();
        //????????????????????? ????????? ?????? ???????????? ?????????
        final AddGroupDialog addGroupDialog= new AddGroupDialog(this, result, new CustomDialogClickListener() {
            @Override
            public void onPositiveClick() {
                if(result.name.length()==0)
                {
                    Toast.makeText(getApplicationContext(), "?????? ????????? ??????????????????",Toast.LENGTH_SHORT).show();
                    return;
                }
                //?????? 0??? ???????????? ?????? ????????????
                if(result.personIDList.size()==0)
                {
                    Toast.makeText(getApplicationContext(), "1??? ?????? ??????????????????",Toast.LENGTH_SHORT).show();
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
                            text.setText("????????? ?????? ??? ?????????\n\n????????? ??????????????????...");
                            alertDialog = builder.create();
                            alertDialog.show();
                        });
                        commServer.registerGroup(LoginActivity.UserEmail, result.name, result.personIDList);
                        groupList.clear();
                        groupList.addAll(commServer.getAllGroups());
                        mHandler.post(() -> {
                            groupAdapter.notifyDataSetChanged();
                            rv.scrollToPosition(groupList.size()-1);
                            Toast.makeText(getApplicationContext(),"?????? ????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        });
                    }
                }.start();

            }
            @Override
            public void onNegativeClick() { }
        });
        //??????????????? ?????? ???????????? ??? ?????????????????? ??????
        addGroupDialog.setCanceledOnTouchOutside(true);
        //???????????? ???????????? ??????????????? ??? ??? ??????
        addGroupDialog.setCancelable(true);
        //????????????
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
            //??????????????? ???????????????
            btn_view.setOnClickListener((View.OnClickListener) view -> {
                if  (isExpanded==false) isExpanded=true;
                else isExpanded=false;
                toggleLayout(isExpanded,itemView,(LinearLayout)itemView.findViewById(R.id.layoutExpand));
            });
        }

    }

     GroupAdapter(ArrayList<GroupData> groupList){
        this.groupList=groupList;
        //???????????? ????????? ?????????(favorites??? 1??????)??? favorites ???????????? ????????? ?????????
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
            if(group.getFavorites()==0) {
                if(favorites.size()>=3){
                    Toast.makeText(view.getContext(), "??????????????? 3????????? ???????????????",Toast.LENGTH_SHORT).show();
                    return;
                }
                group.setFavorites(favorites.size()+1);
                favorites.add(group);
            }
            else {
                Boolean b = favorites.remove(group);

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

        //????????? ?????? ????????? intent??? ????????? ???????????? ????????????
        ibtn_gallery.setOnClickListener(view -> {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"image/*");
            view.getContext().startActivity(intent);
        });

        //??????????????? ????????? ??????????????? ????????? ??????????????? ?????????. ????????? ????????? ?????? ???????????? ???
        ibtn_edit.setOnClickListener(view -> {
            Toast.makeText(holder.view.getContext(),"????????? ?????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();

            et_name.setEnabled(true);
            //????????? ???
            et_name.requestFocus();
            et_name.setSelection(et_name.length());
            ibtn_check.setVisibility(View.VISIBLE);
            ibtn_edit.setVisibility(View.GONE);

            //????????? ????????? ??????
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        });
        //??????????????? ????????? ?????? ????????? ???????????? DB??? ?????????. ??????????????? ???????????? ?????? ??????????????? ?????????
        ibtn_check.setOnClickListener(view -> {

            group.setName(et_name.getText().toString());
            new Thread(){
                @Override
                public void run() {
                    commServer.editGroup(group.getId(), et_name.getText().toString(), null, null);
                }
            }.start();
            Toast.makeText(holder.view.getContext(),"?????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
            et_name.setEnabled(false);
            ibtn_check.setVisibility(View.GONE);
            ibtn_edit.setVisibility(View.VISIBLE);
        });

        //??????X????????? ????????? ??? ?????? (????????? ?????????)
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
                            Toast.makeText(holder.view.getContext(),"?????? ????????? ??????????????????!", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        });
                    }
                }.start();
            }
        });

        ImageButton ibtn_editGroup = holder.view.findViewById(R.id.ibtn_editGroupPerson);
        ibtn_editGroup.setOnClickListener(view -> {

            //????????????????????? ????????? ?????? ???????????? ?????????(???????????? ????????? pidList)
            ArrayList<Integer> result = new ArrayList<>();
            ArrayList<Integer> pidList = new ArrayList<>();
            Thread t = new Thread(){
                @Override
                public void run() {
                    pidList.addAll(commServer.getPidListByGid(group.getId()));
                }
            }; t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

            //?????? pid????????????, ?????? pidList??? ????????? result??? ????????? ??????
            final EditGroupDialog editGroupDialog= new EditGroupDialog(view.getContext(), pidList, result, new CustomDialogClickListener() {
                @Override
                public void onPositiveClick() {
                    //?????? 0??? ???????????? ??? ????????????
                    if(result.size()==0)
                    {
                        Toast.makeText(view.getContext(), "1??? ?????? ??????????????????",Toast.LENGTH_SHORT).show();
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
                                    text.setText("????????? ?????? ??? ?????????\n\n????????? ??????????????????...");
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
                                        Toast.makeText(holder.view.getContext(),"?????? ????????? ??????????????????!", Toast.LENGTH_SHORT).show();

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
            //??????????????? ?????? ???????????? ??? ?????????????????? ??????
            editGroupDialog.setCanceledOnTouchOutside(true);
            //???????????? ???????????? ??????????????? ??? ??? ??????
            editGroupDialog.setCancelable(true);
            //????????????
            editGroupDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            editGroupDialog.show();

        });

        //////////////////////////////////
        //??????????????????????????? ?????? ??????
        /////////////////////////////////
        ProfileAdapter profileAdapter;
        ArrayList<PersonData> profileList=new ArrayList<>();
        Thread t = new Thread(){
            @Override
            public void run() {
                profileList.addAll(commServer.getPersonsByGid(group.getId()));
            }
        }; t.start(); try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        //?????????????????? ????????? ??????
        final RecyclerView rv = holder.view.findViewById(R.id.rv_group_profile);
        //???????????? ??????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.view.getContext(),LinearLayoutManager.HORIZONTAL,false);
        rv.setLayoutManager(linearLayoutManager);

        //????????? ?????? ??? ?????????????????? ???????????? ??????
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

        //????????? ?????? ????????? ?????? ????????? ??????
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

