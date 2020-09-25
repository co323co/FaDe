package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.model.Image;
import com.example.fade.DB.DBThread;
import com.example.fade.DB.entity.Person;
import com.example.fade.Server.ConnService;
import com.example.fade.Server.ReturnData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/////////////////////////////////////////////////////////////
//기존 갤러리2액티비티입니다
//갤러리에서 고른 사진 재확인 후 서버에 올리는 액티비티
/////////////////////////////////////////////////////////////

public class RegiPersonActivity2 extends AppCompatActivity {
    ArrayList<Bitmap> bitmaps; //갤러리사진

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetPermission.verifyStoragePermissions(this);
        setContentView(R.layout.activity_regiperson2);
        setTitle("재확인");

        ConvertFile convertFile = new ConvertFile(getApplicationContext());

        bitmaps = new ArrayList<Bitmap>();
        List<Image> images = getIntent().getParcelableArrayListExtra("images");
        MyGridAdapter gAdapter = new MyGridAdapter(this, images);
        final GridView gv = (GridView) findViewById(R.id.gv_regiperson2);
        gv.setAdapter(gAdapter);


        Button btn = (Button)findViewById(R.id.btn_regiperson2);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService ConnService = retrofit.create(ConnService.class);
        btn.setOnClickListener(view -> {

            btn.setClickable(false);
            ProgressBar pb = (ProgressBar)findViewById(R.id.pb_loading_regiperson2);
            TextView tv = (TextView)findViewById(R.id.tv_regiperson2);
            pb.setVisibility(View.VISIBLE);
            tv.setVisibility(View.VISIBLE);

            ///////////////////내부 DB저장 코드
            String profile_name = getIntent().getExtras().getString("profile_name");
            byte[] profile_thumbnail = getIntent().getExtras().getByteArray("profile_thumbnail");
            if(profile_thumbnail==null) Log.d("RegiPersonActivity2", "profile_thumbnail is null, 프로필 선택 안함");
            else  Log.d("RegiPersonActivity2", "profile_thumbnail 길이 : " + profile_thumbnail.length );

            Person person = new Person(profile_name, profile_thumbnail);

            DBThread.InsertPersonThraed t1 = new DBThread.InsertPersonThraed(person);
            t1.start();
            try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }

            int[] pid = new int[1];
            DBThread.SelectRecentlyPIDThread t2 = new DBThread.SelectRecentlyPIDThread(pid);
            t2.start();
            try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
            ////////////////////////////////////////////


            /////////////////////////////////////////////////////////////////////
            //서버에 이미지들을 보내는 코드
            /////////////////////////////////////////////////////////////////////
           Thread t = new Thread()
           {
               @Override
               public void run() {

                   Log.d("testtest", "실행시작");
                   //이미지들 비트맵으로 변환
                   for (Image image : images) {

                       Uri uri = image.getUri();
                       String rotation = convertFile.getRotationOfAllImage(uri);
//                    Log.d("testtest", "get로테완료");

//                       Bitmap bm = null;
                    Bitmap bm  = convertFile.resize(uri, 200);
//                       Bitmap bm = null;
//                       try {
//                           bm = new Resizer(getApplicationContext())
//                                   .setTargetLength(500)
//                                   .setSourceImage(new File(getRealPathFromURI(uri)))
//                                   .getResizedBitmap();
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                       }
//                       try {
//                           bm  =  MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                       }
                       Log.d("testtest", "resize 완료");
//                    Log.d("testtest", "uri to bm 완료");

                       Bitmap bmRotated=null;
                       try{
                           bmRotated = convertFile.rotateBitmap2(bm, rotation); //bitmap 사진 파일(bitmap형태의)i
//                        Log.d("testtest", "rotate 완료");
                       }
                       catch (Exception e) {Log.e("testtest", "rotateBitmap2 에러 :: " + e.toString());}
                       bitmaps.add(bmRotated);
                   }
                   Log.d("testtest", "실행완료");

                   /////////////API 낮은버전 (혹시모르니 지우지말자)
//                    ExifInterface exif = null; // 회전값
//                    try {
//                        exif = new ExifInterface(filePath);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//                    Bitmap bmRotated = rotateBitmap(bm, orientation); //bitmap 사진 파일(bitmap형태의)i

                   //////////////////비트맵들을 이진파일들로 변환
                   ArrayList<byte[]> byteList=new ArrayList<byte[]>();

                   ConvertFile.bitmapsToByteArrayThread t = convertFile.new bitmapsToByteArrayThread(bitmaps,byteList);
                   t.start();
                   try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                   //바이트사진들 -> base64String으로 인코딩
                   //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
                   JSONObject enPicureList = new JSONObject();
                   for(int i=0; i< byteList.size();i++){ try { enPicureList.put("byte_"+i, Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

                   //////////////////
                   HashMap<String, Object> rp_input = new HashMap<>();
                   rp_input.put("uid", LoginActivity.UserID);
                   rp_input.put("pid", pid[0]);
                   rp_input.put("pictureList", enPicureList);

                   ConnService.postRegisterPerson(rp_input).enqueue(new Callback<ReturnData>() {
                       @Override
                       public void onResponse(Call<ReturnData> call, Response<ReturnData> response) {
                           if (response.isSuccessful()) {
                               ReturnData body = response.body();
                               if (body != null) { Log.d("server", "사진 던져주기 성공 (postRegisterPerson)"); } }
                       }
                       @Override
                       public void onFailure(Call<ReturnData> call, Throwable t) {
                           Log.e("server", "postRegisterPerson : " + t.getMessage()); }
                   });
                   Log.d("testtest", "서버전송 완료");
                   Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                   startActivity(intent);
                   finish();
               }
           };
            t.start();
//            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        });


    }

    public class MyGridAdapter extends BaseAdapter {
        Context mcontext;
        List<Image> mimages;

        public MyGridAdapter(Context c, List<Image> images) {
            mcontext = c;
            mimages = images;
        }

        @Override
        public int getCount() {
            return mimages.size();
        }

        @Override
        public Object getItem(int position) {
            return mimages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView ivUser;
            if (convertView == null) {
                ivUser = new ImageView(mcontext);
                ivUser.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                //ivUser.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivUser.setPadding(3, 0, 3, 0);
            } else {
                ivUser = (ImageView) convertView;
            }
            Log.d(String.valueOf(mimages.get(position).getUri()), "images uri");
            Uri uri = Uri.parse(mimages.get(position).getUri().toString());

            Glide.with(mcontext)
                    .load(uri)
                    .override(500)
                    .centerCrop()
                    .into(ivUser);
//            Log.d(getRealPathFromURI(uri), "이미지 경로");

            return ivUser;
        }
    }


}