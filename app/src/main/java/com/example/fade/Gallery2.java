package com.example.fade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.esafirm.imagepicker.model.Image;
import java.util.List;

public class Gallery2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_photo);
        setTitle("재확인");
        Intent intent = getIntent();
        List<Image> images = getIntent().getParcelableArrayListExtra("images");
        //Log.d(images.get(0).getUri().toString(),"images");
        //Log.d(images.get(1).toString(),"images");
        MyGridAdapter gAdapter = new MyGridAdapter(this,images);
        final GridView gv = (GridView)findViewById(R.id.folder_gridView);
        gv.setAdapter(gAdapter);



    }
    public class MyGridAdapter extends BaseAdapter{
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
            if(convertView == null){
                ivUser = new ImageView(mcontext);
                //ivUser.setLayoutParams(new GridView.LayoutParams(400,350));
                //ivUser.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivUser.setPadding(5,5,5,5);
            } else {
                ivUser = (ImageView) convertView;
            }
            Log.d(String.valueOf(mimages.get(position).getUri()), "images");
            Uri uri = Uri.parse(mimages.get(position).getUri().toString());
            //for (int i = 0; i < mimages.size(); i++) {

            //}
            ivUser.setImageURI(uri);

            return ivUser;


        }
    }


}
