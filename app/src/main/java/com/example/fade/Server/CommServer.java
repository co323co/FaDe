package com.example.fade.Server;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.fade.DB.DBThread;
import com.example.fade.LoginActivity;
import com.example.fade.MainActivity;
import com.example.fade.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommServer {
    public Context context;
    ArrayList<String> jsonresult = new ArrayList<>();
    ArrayList<Uri> uriArrayList_;
    public  CommServer(Context context)
    {
        this.context=context;
    }

    public CommServer(Context context, ArrayList<Uri> uriArrayList)
    {
        this.context = context; this.uriArrayList_ = uriArrayList;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void postDB() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        String path  = context.getDataDir()+"/databases/";

        String[] pathList = {"App.db", "App.db-shm","App.db-wal"};

        ArrayList<byte[]> byteList = new ArrayList<>();

        for(int i=0; i<pathList.length;i++)
        {
            File db = new File(path+pathList[i]);
            FileInputStream fileInputStream = new FileInputStream(db);
            byteList.add(inputStreamToByteArray(fileInputStream));
            fileInputStream.close();

        }
        //바이트사진들 -> base64String으로 인코딩
        //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
        JSONObject enDBFiles = new JSONObject();
        for(int i=0; i< byteList.size();i++){ try { enDBFiles.put(pathList[i], Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

        HashMap<String, Object> input = new HashMap<>();
        input.put("dbFiles", enDBFiles);

        connService.postDB(LoginActivity.UserID, input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d("server", "통신성공 (putDB) : " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("server", "통신실패 (putDB) : " + t.getMessage()+"" );
            }
        });
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

        //바이트사진들 -> base64String으로 인코딩
        //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
        JSONObject enFiles = new JSONObject();
        for(int i=0; i< byteList.size();i++){ try { enFiles.put(i+".jpg", Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

        HashMap<String, Object> input = new HashMap<>();
        input.put("GalleryFiles", enFiles);
        Log.i("updateGalleryImg ", "GalleryFiles 묶기 완료");

        //selectGalleryImage selectGalleryImage = new selectGalleryImage(context);
        connService.postDetectionPicture(LoginActivity.UserID, input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    jsonresult = getJSONdata(response.body().string());
                    moveGalleryImage(jsonresult, uriArrayList);
                    Toast.makeText(context,"이미지 분류 완료", Toast.LENGTH_SHORT ).show();
                    if(MainActivity.CONTEXT!=null){
                        ((MainActivity)(MainActivity.CONTEXT)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("server", "통신실패 (postDetectionPicture) : " + t.getMessage()+"" );
                Toast.makeText(context,"통신실패", Toast.LENGTH_SHORT ).show();
                ((MainActivity)(context)).mMenu.findItem(R.id.menu_galleryRefresh).setActionView(null);
            }
        });
    }


//        <<그룹등록>>
//        서버에 uid, gid, (그룹의)pidList 던져주는 함수
//         (서버 : pid폴더들에서 사진 찾아내서 그룹단위 모델을 학습함 -> uid/group_model 폴더에 모델 저장
    public void postRegisterGroup(String uid ,int gid, ArrayList<Integer> pidList){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> rg_input = new HashMap<>();
        rg_input.put("uid", uid);
        rg_input.put("gid", gid);
        rg_input.put("pidList", pidList);

        connService.postRegisterGroup(rg_input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) { Log.d("server", "그룹 등록하기 성공 (postRegisterGroup)"); }
                    Toast.makeText(context,"그룹 등록을 성공했습니다!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.e(t.toString(), "통신실패 (postRegisterGroup)"+t.getMessage());
                Toast.makeText(context,"통신실패", Toast.LENGTH_SHORT).show();
            }

        });
    }

 //        <<그룹편집>>
//        서버에 uid, gid, (그룹의)pidList 던져주는 함수
//         (서버 : 그룹모델 업데이트)
    public void postEditGroup(String uid ,int gid, ArrayList<Integer> pidList, int pid){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("uid", uid);
        input.put("gid", gid);
        input.put("pidList", pidList);
        input.put("pid", pid);

        connService.postEditGroup(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) { Log.d("server", gid + "그룹 편집하기 성공 (postEditGroup)"); } }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.e(t.toString(), "통신실패 (postRegisterGroup)"+t.getMessage()); }
        });

    }
    public void DeleteGroup(String uid ,int gid, int pid){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("uid", uid);
        input.put("gid", gid);
        input.put("pid", pid);

        connService.DeleteGroup(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) { Log.d("server", "그룹 삭제 성공 (postEditGroup)"); } }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.e(t.toString(), "통신실패 (postRegisterGroup)"+t.getMessage()); }
        });

    }
    public void DeletePerson(String uid , int pid){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        HashMap<String, Object> input = new HashMap<>();
        input.put("uid", uid);
        input.put("pid", pid);

        connService.DeletePerson(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) { Log.d("server", "사람 삭제 성공 (postEditGroup)"); } }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.e(t.toString(), "통신실패 (postRegisterGroup)"+t.getMessage()); }
        });

    }

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

//서버에서 받은 gid를 가지고 gname 리스트를 만드는 함수(그룹에 해당되지 않는 사진은 None으로 반환)
    public ArrayList<String> getJSONdata(String data){

        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<String> gnameResult = new ArrayList<>();
        try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("gid_list");
                if (jsonArray != null) {
                    for (int i=0;i<jsonArray.length();i++){
                        result.add(jsonArray.getInt(i));
                        if(result.get(i) == -1){
                            gnameResult.add("None");
                        }
                        else{
                            DBThread.SelectGnameThraed t1 = new DBThread.SelectGnameThraed(result.get(i), gnameResult);

                            t1.start();
                            try {
                                t1.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Log.i("server", "통신성공 (postDetectionPicture -> getJSONdata) : " + result+"    "+gnameResult);


        } catch (Exception e) {
            e.printStackTrace();
        }return gnameResult;
    }

    //gname리스트와 받아온 uri 리스트를 통해 gname에 해당하는 폴더로 경로 이동(None일 경우 폴더 변경x pass)
    public boolean moveGalleryImage(ArrayList<String> gname, ArrayList<Uri> uriArrayList)
    {

        String relativeLocation = Environment.DIRECTORY_PICTURES+ File.separator+"FADE"+ File.separator;

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        for(int i = 0; i<uriArrayList.size();i++){
            if(gname.get(i)=="None"){
                    continue;
            }
            else{
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation+gname.get(i));
                contentResolver.update(uriArrayList.get(i), contentValues,null,null);
            }

        }
        Log.i(uriArrayList.size()+"개의 사진 업데이트 완료함", "므엥");

        return true;
    }
}
