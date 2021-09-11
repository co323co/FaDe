package com.example.fade.Tutorial;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fade.R;

public class ViewHolderPage extends RecyclerView.ViewHolder {

    private TextView tv_title;
    private ImageView imageView;
    private TextView num;
    DataPage data;

    ViewHolderPage(View itemView) {
        super(itemView);
        tv_title = itemView.findViewById(R.id.title);
        imageView = itemView.findViewById(R.id.image);
        num = itemView.findViewById(R.id.number);
    }

    public void onBind(DataPage data){
        this.data = data;

        tv_title.setText(data.getTitle());
        imageView.setBackgroundResource(data.getImage());
        num.setText(data.getNum()+"");
    }
}