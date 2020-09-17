package com.example.fade;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ConvertFile {

    Context context;

    public  ConvertFile(Context context){
        this.context = context;
    }

    //비트맵을 바이트로 바꿔주는  스래드
    class bitmapsToByteArrayThread extends Thread
    {
        ArrayList<Bitmap> bitmaps;
        ArrayList<byte[]> byteList;

        public bitmapsToByteArrayThread(ArrayList<Bitmap> bitmaps, ArrayList<byte[]> byteList)
        {
            this.bitmaps=bitmaps;
            this.byteList=byteList;
        }
        @Override
        public void run() {
            super.run();

            for(int i=0; i<bitmaps.size();i++)
            {
                System.out.println(i);
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                byteList.add(byteArray.toByteArray());
                try { byteArray.close(); } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("bitmapsToByteArrayThread",e.toString());
                }
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

    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
        return bitmap ;
    }

    public byte[] UriToByteArray(Context context, Uri uri) {

        Bitmap bm = null; //Bitmap 로드
        try { bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri); } catch (IOException e) { e.printStackTrace(); }
        ByteArrayOutputStream  byteArray = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        return byteArray.toByteArray();

    }

    // 이미지 사이즈 조절 함수
    public Bitmap resize(Uri uri,int resize){
        Bitmap resizeBitmap=null;

        if(uri == null) return null;

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

    public String getRotationOfAllImage(Uri uri)
    {
        String result = null;
//        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.ORIENTATION
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

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


}
