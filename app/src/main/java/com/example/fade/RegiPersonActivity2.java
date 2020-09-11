package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.fade.Server.ConnService;
import com.example.fade.Server.ReturnData;
import com.example.fade.DB.entity.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.echodev.resizer.Resizer;
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
            Person person = new Person(profile_name);

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
                       String rotation = getRotationOfAllImage(uri);
//                    Log.d("testtest", "get로테완료");

//                       Bitmap bm = null;
//                    Bitmap bm  = resize(getApplicationContext(), uri, 200);
                       Bitmap bm = null;
                       try {
                           bm = new Resizer(getApplicationContext())
                                   .setTargetLength(500)
                                   .setSourceImage(new File(getRealPathFromURI(uri)))
                                   .getResizedBitmap();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
//                       try {
//                           bm  =  MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
//                       } catch (IOException e) {
//                           e.printStackTrace();
//                       }
                       Log.d("testtest", "resize 완료");
//                    Log.d("testtest", "uri to bm 완료");

                       Bitmap bmRotated=null;
                       try{
                           bmRotated = rotateBitmap2(bm, rotation); //bitmap 사진 파일(bitmap형태의)i
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

                   ConvertFile convertFile  = new ConvertFile();
                   ConvertFile.bitmapsToByteArrayThread t = convertFile.new bitmapsToByteArrayThread(getApplicationContext(),bitmaps,byteList);
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

    private String getRotationOfAllImage(Uri uri)
    {
        String result = null;
//        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.ORIENTATION
        };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

        int columnID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int columnDisplayname = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);//파일 이름 메타데이터에서 가져오기
        int columnRotation = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.ORIENTATION);

        int i = 0;

        while (cursor.moveToNext())
        {
            String nameOfFile = cursor.getString(columnDisplayname);
            String rotationOfImage = cursor.getString(columnRotation);
            Long id = cursor.getLong(columnID);
            Uri contentUri = Uri.withAppendedPath(uri, id.toString());

            if (contentUri!=null) {//이미지 파일목록을 싹 돈다(언니 이쪽 부분만 바꾸면돼! 나만 쓰고 언니가 안쓰는 변수명은 지우고 행(날짜같은거))
                if(rotationOfImage==null) result = "0";
                else {
                    result=rotationOfImage ;
                    Log.d("rotatest",nameOfFile + " :: " + rotationOfImage);
                }
                i++;
            }
        }
        cursor.close();
        return result;
    }

    //API 10.0 미만 사용
    // 이미지 회전 함수
    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {

            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;

            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //API 10.0부터 사용
    // 이미지 회전 함수
    public Bitmap rotateBitmap2(Bitmap bitmap, String orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {

            case "0" :
                return bitmap;

            case "180":
                matrix.setRotate(180);
                break;

            case "90":
                matrix.setRotate(90);
                break;

            case "270":
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    // 이미지 경로 구하는 함수
    private String getRealPathFromURI(Uri contentURI) {
        String filePath;
        Cursor cursor = RegiPersonActivity2.this.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            filePath = contentURI.getPath();
        }
        else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            filePath = cursor.getString(idx);
            cursor.close();
        }
        return filePath;
    }

    // 이미지 사이즈 조절 함수
    private Bitmap resize(Context context,Uri uri,int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
         BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }


}