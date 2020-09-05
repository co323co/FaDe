package com.example.fade;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ConvertFile {

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

    public byte[] UriToByteArray(Context context, Uri uri) {

        Bitmap bm = null; //Bitmap 로드
        try { bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri); } catch (IOException e) { e.printStackTrace(); }
        ByteArrayOutputStream  byteArray = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        return byteArray.toByteArray();

    }
}
