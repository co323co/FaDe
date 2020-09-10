package com.example.fade.Server;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.fade.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

    public  CommServer(Context context)
    {
        this.context=context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void postDB() throws IOException {


//        Log.d("server", ""+context.getDataDir()+"/databases/");
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
    public void postGalleryImg(ArrayList<byte[]> imgpathList) throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

//        Log.d("server", ""+context.getDataDir()+"/databases/");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        final ConnService connService = retrofit.create(ConnService.class);

        ArrayList<byte[]> byteList = imgpathList;
        Log.i("pathList 받아오기 성공 ", byteList.toString());

        //바이트사진들 -> base64String으로 인코딩
        //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
        JSONObject enDBFiles = new JSONObject();
        for(int i=0; i< byteList.size();i++){ try { enDBFiles.put(i+".jpg", Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

        HashMap<String, Object> input = new HashMap<>();
        input.put("GalleryFiles", enDBFiles);
        Log.i("pathList 묶기 완료 ", "므엑");
        connService.postGalleryImg("20171108", input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.i("server", "통신성공 (putDB) : " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("server", "통신실패 (putDB) : " + t.getMessage()+"" );
            }
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

}
