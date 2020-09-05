package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.model.Image;
import com.example.fade.Server.ConnService;
import com.example.fade.Server.ReturnData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Gallery2 extends AppCompatActivity {
    private final int REQUEST_WIDTH = 512;
    private final int REQUEST_HEIGHT = 512;
    ArrayList<Bitmap> bitmaps; //갤러리사진

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_photo);
        setTitle("재확인");
        bitmaps = new ArrayList<Bitmap>();
        Intent intent = getIntent();
        List<Image> images = getIntent().getParcelableArrayListExtra("images");

        //Log.d(images.get(0).getUri().toString(),"images");
        //Log.d(images.get(1).toString(),"images");
        MyGridAdapter gAdapter = new MyGridAdapter(this, images);
        final GridView gv = (GridView) findViewById(R.id.folder_gridView);
        gv.setAdapter(gAdapter);


        Button btn1 = (Button)findViewById(R.id.button);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConnService.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ConnService ConnService = retrofit.create(ConnService.class);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<byte[]> byteList=new ArrayList<byte[]>();

                Log.e("ㅂㅁㄴㅇㄹ", bitmaps.toString());

                //비트맵들을 이진파일들로 변환
                bitmapsToByteArrayThread t = new bitmapsToByteArrayThread(getApplicationContext(),bitmaps,byteList);
                t.start();
                try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                //바이트사진들 -> base64String으로 인코딩
                //사진바이트리스트를 JSON으로 파이썬에 던져주기 위해서 base64로 인코딩해서 JOSNobject로 만들었음.
                JSONObject enPicureList = new JSONObject();
                for(int i=0; i< byteList.size();i++){ try { enPicureList.put("byte_"+i, Base64.encodeToString(byteList.get(i), Base64.NO_WRAP)); } catch (JSONException e) { e.printStackTrace();} }

                //////////////////////////////////


                HashMap<String, Object> rp_input = new HashMap<>();
                rp_input.put("uid", "20171218");
                rp_input.put("pid", 1);
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

                    }
                });

            }
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
                //ivUser.setLayoutParams(new GridView.LayoutParams(400,350));
                //ivUser.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivUser.setPadding(5, 5, 5, 5);
            } else {
                ivUser = (ImageView) convertView;
            }
            Log.d(String.valueOf(mimages.get(position).getUri()), "images uri");
            Uri uri = Uri.parse(mimages.get(position).getUri().toString());


            //return ivUser;
            //ivUser.setImageURI(uri);


            Glide.with(mcontext)
                    .load(uri)
                    .override(500)
                    .fitCenter()
                    .into(ivUser);
            Log.d(getRealPathFromURI(uri), "이미지 경로");

            Bitmap bm = null;
            bm = resize(getApplicationContext(), uri, 100);
            bitmaps.add(bm);
            /*
            try {
                bm = resize(getApplicationContext(), uri, 200);
                bm = MediaStore.Images.Media.getBitmap(mcontext.getContentResolver(), uri);
                bitmaps.add(bm);
                Log.e(bitmaps.toString(),"ㅁㄴㅇㄹㅁㄴㅇㄹㅁㄴㅇㄹ");
            } catch (IOException e) {
                e.printStackTrace();
            }
/*
            /*try {
                Bitmap bm, br = null;
                bm = MediaStore.Images.Media.getBitmap(mcontext.getContentResolver(), uri);
                br = resize(mcontext, uri, 200);

                ivUser.setImageBitmap(br);
                //Log.d(br.getPixels, "images uri");

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */
            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(mcontext.getContentResolver().openInputStream(uri), null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            String imageType = options.outMimeType;
            options.inSampleSize = calculateInSampleSize(options, 100, 100);*/




            return ivUser;
        }


        // 이미지 경로 구하는 함수
        private String getRealPathFromURI(Uri contentURI) {
            String filePath;
            Cursor cursor = Gallery2.this.getContentResolver().query(contentURI, null, null, null, null);
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






        private int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }



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

}
//비트맵을 바이트로 바꿔주는  스래드
class bitmapsToByteArrayThread extends Thread
{
    Context context;
    ArrayList<Bitmap> bitmaps;
    ArrayList<byte[]> byteList;

    public bitmapsToByteArrayThread(Context context, ArrayList<Bitmap> bitmaps, ArrayList<byte[]> byteList)
    {
        this.context=context;
        this.bitmaps=bitmaps;
        this.byteList=byteList;
    }
    @Override
    public void run() {
        super.run();

        for(int i=0; i<bitmaps.size();i++)
        {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
            byteList.add(byteArray.toByteArray());
//            writeToFile(i+".png",byteList.get(i));

        }

    }

    //바이트array를 파일로 저장하게 해주는 함수
//    public void writeToFile(String filename, byte[] pData) {
//        if(pData == null){ return; }
//        int lByteArraySize = pData.length;
//        System.out.println(filename);
//        try{
//            File lOutFile = new File(context.getFilesDir()+filename);
//            FileOutputStream lFileOutputStream = new FileOutputStream(lOutFile);
//            lFileOutputStream.write(pData);
//            lFileOutputStream.close();
//        }catch(Throwable e){
//            e.printStackTrace(System.out);
//            Log.d("data",e.toString());
//        }
//    }

}
