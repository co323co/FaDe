package com.example.fade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ConvertFile {

    //비트맵을 바이트로 바꿔주는  스래드

    class bitmapsToByteArrayThread extends Thread
    {
        ArrayList<Bitmap> bitmaps;
        ArrayList<byte[]> byteList;
        Context context;

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
                try { byteArray.close(); } catch (IOException e) { e.printStackTrace();
                    Log.e("bitmapsToByteArrayThread",e.toString());}
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

    public byte[] UriToByteArray(Context context, Uri uri) {

        Bitmap bm = null; //Bitmap 로드
        try { bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri); } catch (IOException e) { e.printStackTrace(); }
        ByteArrayOutputStream  byteArray = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        return byteArray.toByteArray();

    }

    // 이미지 사이즈 조절 함수
    public Bitmap resize(Context context,Uri uri,int resize){
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
