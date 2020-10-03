package com.example.fade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GalleryUpdate {
    public ArrayList<Uri> groupUriList;
    public Context context;
    public GalleryUpdate(Context context){
        this.context = context;
    }

    public GalleryUpdate(Context context, ArrayList<Uri> groupUriList){
        this.groupUriList = groupUriList;
        this.context = context;
    }
    public ArrayList<byte[]> getByteArrayOfRecentlyImages()   //최근 갤러리 이미지 가져오기
    {
        Uri uri;
        SimpleDateFormat dateFormat;
        ArrayList<byte[]> byteList = new ArrayList<byte[]>();
        groupUriList = new ArrayList<>();
        String last_update; //제일 마지막에 업뎃한 시간
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        last_update = "2020/9/28";

        ConvertFile convertFile  = new ConvertFile(context);

        String[] projection = {
//                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
        };

        String where = MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'";
        Cursor cursor = context.getContentResolver().query(uri, projection, where, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");


//        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA); //절대경로 메타데이터에서 가져오기
        int columnDisplayname = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);//파일 이름 메타데이터에서 가져오기
        int columnDate = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED); //생성된 날짜 메타데이터에서 가져오기
        int columnId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID); //생성된 날짜 메타데이터에서 가져오기

        int lastIndex;

        dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        int i = 0;

        ArrayList<Bitmap>bitmaps = new ArrayList<Bitmap>();
        while (cursor.moveToNext())
        {

            Long IdOfImage = cursor.getLong(columnId);
            Uri  uriimage = Uri.withAppendedPath(uri,""+IdOfImage);


            String nameOfFile = cursor.getString(columnDisplayname);
            String DateOfImage = dateFormat.format(new Date(cursor.getLong(columnDate) * 1000L));
            ContentResolver contentResolver = context.getContentResolver();

            int compare_time_last = DateOfImage.compareTo(last_update);//사진이 생성된 날짜와 마지막 업뎃 날짜를 비교하여
            if(compare_time_last>=0){
                try{
                    bitmaps.add(convertFile.resize(uriimage, 200));
                    groupUriList.add(uriimage);
                    i++;

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        cursor.close();

        //리사이즈된 비트맵들을 바이트들로 바꿔줌
        ConvertFile.bitmapsToByteArrayThread t = convertFile.new bitmapsToByteArrayThread(bitmaps,byteList);
        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        Log.i("getByteArrayOfRecentlyImages", i+"개의 사진의 바이트가 담긴 리스트 리턴함");

//업데이트한 날짜로 마지막에 업데이트한 날짜 바꿔주기!ㄱ
        //last_update = dateFormat.format(new Date());
        return byteList;
    }

}
