package com.example.fade;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileAccessObject {

    //프로필 설정 함수
    static void setProfile(Context context, int personID, Uri imgUri) {

        String fpath = getPathFromUri(context, imgUri);
        File mPath = context.getDir("profile", Context.MODE_PRIVATE);
        try {
            FileOutputStream fout = new FileOutputStream(mPath + "/" + personID);
            byte[] b = fpath.getBytes();
            fout.write(b);
            fout.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "FileNotFoundException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "IOException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    static String getProfile(Context context, int personID) {

        File mPath = context.getDir("profile", Context.MODE_PRIVATE);
        try {
            FileInputStream fin = new FileInputStream(mPath + "/" + personID);

            //Reader를 사용해서 깨짐 현상 해결
            Reader reader = new InputStreamReader(fin);
            BufferedReader in = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();

            int c = 0;
            while ((c = in.read()) != -1)
                builder.append((char) c);

            fin.close();

            return builder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    static void copyFile(Context context, File origin, File copy) {

        File mPath = context.getDir("profile", Context.MODE_PRIVATE);
        try {
            FileInputStream fin = new FileInputStream(origin.toString());
            FileOutputStream fout = new FileOutputStream(copy.toString());

            int n = 0;
            byte buf[] = new byte[1024];

            while ((n = fin.read(buf)) != -1) { // 여기서 n은 읽은 바이트 수
                fout.write(buf, 0, n); // 쓰는게 아니라 스트림에 넣는느낌
                fout.flush(); // 데이터를 직접 파일에다가 쓰는 과정 (스트림에 있는걸 이제 적용)
            }
            fin.close();
            fout.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //path를 이미지뷰에 붙여준다
    static void FileAsView(Context context, ImageView imageView, String path) {

        File file = new File(path);
        try {
            if(file.exists()==true) {
                Toast.makeText(context,file.toString(), Toast.LENGTH_SHORT).show();
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
            else
                Toast.makeText(context,"파일경로널", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(context,"예외", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    static String getPathFromUri(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        cursor.moveToNext();

        String path = cursor.getString(cursor.getColumnIndex("_data"));

        cursor.close();

        return path;

    }

}

