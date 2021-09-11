package com.example.fade;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class GalleryUpdate {
    public ArrayList<Uri> groupUriList;
    public Context context;
    String last_update;
    long last_update_Timestamp;
    public GalleryUpdate(Context context){
        this.context = context;
    }

    public GalleryUpdate(Context context, ArrayList<Uri> groupUriList, String last_update){
        this.groupUriList = groupUriList;
        this.context = context;
        this.last_update = last_update;

    }
    public ArrayList<byte[]> getByteArrayOfRecentlyImages()   //최근 갤러리 이미지 가져오기
    {
        Uri uri;
        SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy/MM/dd");
        try { last_update_Timestamp = (dateFormat.parse(last_update)).getTime()/1000; } catch (ParseException e) { e.printStackTrace(); }
        ArrayList<byte[]> byteList = new ArrayList<>();
        groupUriList = new ArrayList<>();
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ConvertFile convertFile  = new ConvertFile(context);

        String[] projection = {
//                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
        };

        //last_update 이후의 사진만 가져옴
        String where = MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'" + " and " + MediaStore.MediaColumns.DATE_ADDED  + ">" + "'" + last_update_Timestamp  +"'";
        Cursor cursor = context.getContentResolver().query(uri, projection, where, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");


//        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA); //절대경로 메타데이터에서 가져오기
        int columnDisplayname = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);//파일 이름 메타데이터에서 가져오기
        int columnDate = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED); //생성된 날짜 메타데이터에서 가져오기
        int columnId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID); //생성된 날짜 메타데이터에서 가져오기

        int lastIndex;

        int i = 0;

        ArrayList<Bitmap>bitmaps = new ArrayList<Bitmap>();
        while (cursor.moveToNext())
        {

            Long IdOfImage = cursor.getLong(columnId);
            Uri  uriimage = Uri.withAppendedPath(uri,""+IdOfImage);


            String nameOfFile = cursor.getString(columnDisplayname);
            String DateOfImage = dateFormat.format(new Date(cursor.getLong(columnDate) * 1000L));
            ContentResolver contentResolver = context.getContentResolver();

            try{
                bitmaps.add(convertFile.resize(uriimage, 200));
                groupUriList.add(uriimage);
                i++;

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        cursor.close();
        //리사이즈된 비트맵들을 바이트들로 바꿔줌
        ConvertFile.bitmapsToByteArrayThread t = convertFile.new bitmapsToByteArrayThread(bitmaps,byteList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        Log.i("getByteArrayOfRecentlyImages", i+"개의 사진의 바이트가 담긴 리스트 리턴함");

////업데이트한 날짜로 마지막에 업데이트한 날짜 바꿔주기! -> 서버로 따로 빼냄
//        last_update = dateFormat.format(new Date());
//        SharedPreferences.Editor editor = context.getSharedPreferences("alarm_check", MODE_PRIVATE).edit();
//        editor.putString("last_update", last_update);
//        editor.commit();
//        Log.e("마지막 업데이트 날짜 현재로 변경",last_update);
        return byteList;
    }

}
