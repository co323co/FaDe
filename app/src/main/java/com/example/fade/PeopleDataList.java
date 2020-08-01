package com.example.fade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import java.io.File;
import java.util.Vector;

class PersonData {

    static int count = 0;
    int id;
    String name;

    PersonData(String name) {
        this.name = name;
        this.id = count;
        count++;
    }

    Bitmap getProfileBitmap(Context context) {

        if (FileAccessObject.getProfile(context, id) == null) {

            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_people);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            return bitmap;
        }

        File profile = new File(FileAccessObject.getProfile(context, id));

        try {
            if (profile.exists() == true) {

                Bitmap bitmap = BitmapFactory.decodeFile(profile.getAbsolutePath());
                return bitmap;
            } else {
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
    }
}

public class PeopleDataList {

    static private PeopleDataList instance = null;

    //찐 데이터 저장 백터
    private Vector<PersonData> peopleDataList = new Vector<PersonData>();

    //싱글톤
    private PeopleDataList() { }

    public static PeopleDataList getInstance()
    {
        if(instance==null) instance=new PeopleDataList();
        return instance;
    }

    public void addPersonData(PersonData data)
    {
        peopleDataList.add(data);
    }

    public Vector<PersonData> getPeopleDataList()
    {
        return peopleDataList;
    }

}
