package com.example.fade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fade.Tutorial.DataPage;
import com.example.fade.Tutorial.ViewPagerAdapter;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class TutorialActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    Button btn_skip;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        viewPager = findViewById(R.id.viewpager);

        ArrayList<DataPage> list = new ArrayList<>();
        list.add(new DataPage(R.drawable.tutorial_1,"인물을 등록하세요", 1));
        list.add(new DataPage(R.drawable.tutorial_2, "프로필을 등록하세요", 2));
        list.add(new DataPage(R.drawable.tutorial_3, "그룹을 등록하세요", 3));
        list.add(new DataPage(R.drawable.tutorial_4, "그룹에 등록할 인물을 선택하세요", 4));
        list.add(new DataPage(R.drawable.tutorial_5, "갤러리 사진을 그룹별로 정리하세요", 5));
        viewPager.setAdapter(new ViewPagerAdapter(list));

        CircleIndicator3 circleIndicator = findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);
        circleIndicator.createIndicators(list.size(),0);
        btn_skip = findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
