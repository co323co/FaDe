package com.example.fade.Server;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.fade.Alarm.AlarmReceiver;
import com.example.fade.LoginActivity;
import com.example.fade.MainActivity;
import com.example.fade.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/////////////////////네트워크 관련 함수들입니다. MainThread에서 실행하면 오류가 납니다.
////////////////////스래드로 사용해주세요

public class CommServer {
    public Context context;
    ArrayList<Uri> uriArrayList_;
    public  CommServer(Context context)
    {
        this.context=context;
    }

    public CommServer(Context context, ArrayList<Uri> uriArrayList) {
        this.context = context; this.uriArrayList_ = uriArrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void putRegisterUser() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        connService.putRegisterUser(LoginActivity.UserEmail).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("server", "통신성공 (putRegisterUser) ");

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("server", "통신실패 (putRegisterUser) : " + t.getMessage()+"" );
            }
        });
    }

    public ArrayList<GroupData> getAllGroups(){

        final ArrayList<GroupData> groupList = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        Call<List<GroupData>> call = connService.getAllGroups(LoginActivity.UserEmail);

        try {
            List<GroupData> result = call.execute().body();
            groupList.addAll(new ArrayList<>(result));
            Log.d("server", "통신성공(getAllGroups) groupList size : " +  groupList.size());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(getAllGroups) " +  e.getMessage());
        }

        return groupList;
    }

    public ArrayList<PersonData> getAllPersons(){

        final ArrayList<PersonData> personList = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        Call<List<PersonData>> call = connService.getAllPersons(LoginActivity.UserEmail);

        try {
            List<PersonData> result = call.execute().body();
            personList.addAll(new ArrayList<>(result));
            Log.d("server", "통신성공(getAllPersons) personList size : " +  personList.size());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(getAllPersons) " +  e.getMessage());
        }
        return personList;
    }

    public ArrayList<Integer> getPidListByGid(int gid){

        final ArrayList<Integer> pidList = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        Call<List<Integer>> call = connService.getPidListByGid(gid);
        try {
            List<Integer> result = call.execute().body();
            pidList.addAll(new ArrayList<>(result));
            Log.d("server", "통신성공(getPidListByGid) pidList size : " +  pidList.size());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(getPidListByGid) " +  e.getMessage());
        }

        return pidList;
    }

    public ArrayList<PersonData> getPersonsByGid(int gid){

        final ArrayList<PersonData> personList = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        Call<List<PersonData>> call = connService.getPersonsByGid(gid);

        try {
            List<PersonData> result = call.execute().body();
            personList.addAll(new ArrayList<>(result));
            Log.d("server", "통신성공(getPersonsByGid) personList size : " +  personList.size());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(getPersonsByGid) " +  e.getMessage());
        }
        return personList;
    }

    public ArrayList<GroupData> getGroupsByPid(int pid){

        final ArrayList<GroupData> groupList = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        Call<List<GroupData>> call = connService.getGroupsByPid(pid);

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    List<GroupData> result = call.execute().body();
                    groupList.addAll(new ArrayList<>(result));
                    Log.d("server", "통신성공(getGroupsByPid) personList size : " +  groupList.size());

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("server", "통신실패(getGroupsByPid) " +  e.getMessage());
                }
            }
        };
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        return groupList;
    }

    public void registerPerson(String userEmail ,String pname, byte[] thumbnail, ArrayList<byte[]> pictureList){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("userEmail", userEmail);
        input.put("pname", pname);
        if(thumbnail!=null){
            String enThumbnail = Base64.encodeToString(thumbnail, Base64.NO_WRAP);
            input.put("thumbnail", enThumbnail);
        }
        if(pictureList!=null){
            Log.d("server", "pictureList is not null : " + pictureList.size());
            //바이트사진들 -> base64String으로 인코딩
            //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
            JSONObject enPicureList = new JSONObject();
            for(int i=0; i< pictureList.size();i++){ try { enPicureList.put("byte_"+i, Base64.encodeToString(pictureList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

            //byte[]를 String으로 인코딩해서 보내냄. 서버는 String 형태로 보관해야 깨지지 않음
            input.put("pictureList", enPicureList);
        }

        Call<ResponseBody> call = connService.postRegisterPerson(input);
        try {
            call.execute();
            Log.d("server", "인물 등록하기 성공 (postRegisterPerson)");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패 (postRegisterPerson)"+e.getMessage());
        }

    }

    public void registerGroup(String userEmail ,String gname, ArrayList<Integer> pidList){
//        <<그룹등록>>
//        서버에 uid, gid, (그룹의)pidList 던져주는 함수
//         (서버 : pid폴더들에서 사진 찾아내서 그룹단위 모델을 학습함 -> uid/group_model 폴더에 모델 저장
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("userEmail", userEmail);
        input.put("gname", gname);
        input.put("pidList", pidList);

        Call<ResponseBody> call = connService.postRegisterGroup(input);
        try {
            call.execute().body();
            Log.d("server", "그룹 등록하기 성공 (postRegisterGroup)");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(postRegisterGroup) " +  e.getMessage());
        }
    }

    //(수정할 그룹의 gid는 넣어줘야함) 수정할 값만 넣어주고, 유지할 값들은 인자로 null을 넣으면 됨
    public void editGroup(int gid, String gname, ArrayList<Integer> pidList, Integer favorites){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();

        input.put("gid", gid);

        if(gname != null) input.put("gname", gname);
        if(pidList != null) input.put("pidList", pidList);
        if(favorites != null) input.put("favorites", favorites.intValue());

        Call<ResponseBody> call = connService.postEditGroup(input);
        try {
            call.execute();
            Log.d("server", gid + "_그룹 편집하기 성공 (postEditGroup)");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패 (postEditGroup)"+e.getMessage());
        }
    }

    public void deleteGroup(String uid ,int gid){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("userEmail", uid);
        input.put("gid", gid);

        Call<ResponseBody> call = connService.postDeleteGroup(input);
        try {
            call.execute();
            Log.d("server", "그룹 삭제 성공 (deleteGroup)");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패 (deleteGroup)"+e.getMessage());
        }

    }

    public void deletePerson(String userEmail , int pid){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("userEmail", userEmail);
        input.put("pid", pid);

        Call<ResponseBody> call = connService.postDeletePerson(input);
        try {
            call.execute();
            Log.d("server", "사람 삭제 성공 (deletePerson)");

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패 (deletePerson)"+e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateGalleryImg(ArrayList<byte[]> imgByteList, ArrayList<Uri> uriArrayList) throws IOException {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        ArrayList<byte[]> byteList = imgByteList;
        ArrayList<String> gnameList = new ArrayList<>();
        Handler mHandler = new Handler(Looper.getMainLooper());

        //바이트사진들 -> base64String으로 인코딩
        //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
        JSONObject enFiles = new JSONObject();
        for(int i=0; i< byteList.size();i++){ try {
            if(i<10) enFiles.put("0"+i+".jpg", Base64.encodeToString(byteList.get(i), Base64.NO_WRAP));
            else enFiles.put(i+".jpg", Base64.encodeToString(byteList.get(i), Base64.NO_WRAP));
        } catch (JSONException e) { e.printStackTrace();} }

        HashMap<String, Object> input = new HashMap<>();
        input.put("GalleryFiles", enFiles);
        Log.i("updateGalleryImg ", "GalleryFiles 묶기 완료");

        Call<List<String>> call = connService.postDetectionPicture(LoginActivity.UserEmail, input);
        try {
            List<String> result = call.execute().body();
            gnameList.addAll(new ArrayList<>(result));
            Log.d("postDetectionPicture (updateGalleryImg)", "결과 반환" + gnameList);
            moveGalleryImage(gnameList, uriArrayList);
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                  Toast.makeText(context, "이미지 분류 완료", Toast.LENGTH_SHORT).show();
                  if(MainActivity.CONTEXT!=null){
                      ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                  }
                  mHandler.post(new Runnable() {
                      @Override
                      public void run() {

                          AlarmReceiver alarmReceiver = new AlarmReceiver();

                          alarmReceiver.createNotificationChannel(context);

                      }
                  });

              }
            });


        } catch (Exception e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"이미지 분류 실패", Toast.LENGTH_SHORT ).show();
                    ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                    e.printStackTrace();
                    Log.e("updateGalleryImage", "이미지 분류 실패" + e.toString());
                }
            });

        }
    }

/*
        //selectGalleryImage selectGalleryImage = new selectGalleryImage(context);
        connService.postDetectionPicture(LoginActivity.UserEmail, input).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                try {
                    call.execute();
                    //List<String> result = call.execute().body();
                    //gnameList.addAll(new ArrayList<>(result));
                    gnameList.addAll(new ArrayList<>(response.body()));
                    Log.d("postDetectionPicture (updateGalleryImg)", "결과 반환" + gnameList);
                    moveGalleryImage(gnameList, uriArrayList);
                    Toast.makeText(context,"이미지 분류 완료", Toast.LENGTH_SHORT ).show();
                    if(MainActivity.CONTEXT!=null){
                        ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                    }
                } catch (Exception e) {
                    Toast.makeText(context,"이미지 분류 실패", Toast.LENGTH_SHORT ).show();
                    ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.i("server", "통신실패 (postDetectionPicture) : " + t.getMessage()+"" );
                Toast.makeText(context,"통신실패", Toast.LENGTH_SHORT ).show();
                ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
            }
        });
    }*/
    //inputStram을 byte[]로 바꿔준다
    byte[] inputStreamToByteArray(InputStream is) {

        byte[] resBytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int read = -1;
        try {
            while ( (read = is.read(buffer)) != -1 ) {
                bos.write(buffer, 0, read);
            }

            resBytes = bos.toByteArray();
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return resBytes;
    }

    //gname리스트와 받아온 uri 리스트를 통해 gname에 해당하는 폴더로 경로 이동(None일 경우 폴더 변경x pass)
    public boolean moveGalleryImage(ArrayList<String> gname, ArrayList<Uri> uriArrayList)
    {

        String relativeLocation = Environment.DIRECTORY_PICTURES+ File.separator+"FADE"+ File.separator;

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i<uriArrayList.size();i++){
            if(gname.get(i).equals("None")){
                    continue;
            }
            else{
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation+gname.get(i));
                contentResolver.update(uriArrayList.get(i), contentValues,null,null);
            }

        }
        Log.i("moveGalleryImage", uriArrayList.size()+"개의 사진 업데이트 완료함");

        return true;
    }
    public String getLastUpdate(String userEmail){
        String date="";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);
        Call<ResponseBody> call = connService.getLastUpdate(userEmail);
        try {
            date = call.execute().body().string();
            Log.d("server", "통신성공(getLastUpdate) : " +  date);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("server", "통신실패(getLastUpdate) " +  e.getMessage());
        }
        return date;
    }

}
