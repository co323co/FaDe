package com.example.fade;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/////////////////////////////////////////////////////////////
//기존 갤러리1 액티비티입니다
//프로필(activity_regiperson1.xml) 을 등록하고 갤러리로 넘어가 사진들을 고릅니다.
//프로필 정보와 갤러리에서 고른 사진들을 RegiPersonActivity2로 넘깁니다.
/////////////////////////////////////////////////////////////


public class RegiPersonActivity1 extends AppCompatActivity {
    static final int REQ_CODE_SELECT_IMAGE = 42;
    static final int SELECT_IMAGE = 100;
    CircleImageView profile;
    Uri selectedImageUri;
    int btn_choice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiperson1);
        Button profileAdd = (Button) findViewById(R.id.btn_profileAdd);
        Button personAdd = (Button) findViewById(R.id.btn_addPeroson_regiperson1);
        profile = findViewById(R.id.ivUser);

        profileAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_choice = 1;
                hello();
            }
        });
        personAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_choice = -1;
                ImagePicker.create(RegiPersonActivity1.this).start();
            }
        });
    }

    //프로필 사진 선택하기
    public void hello() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, SELECT_IMAGE);
    }



    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (btn_choice == -1) {
            if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                // Get a list of picked images
                List<Image> images = ImagePicker.getImages(data);
                // or get a single image only
                Image image = ImagePicker.getFirstImageOrNull(data);
                EditText person_name = findViewById(R.id.et_profile_name_regiperson1);

                Intent intent = new Intent(RegiPersonActivity1.this, RegiPersonActivity2.class);
                Log.d("filetest",images.size()+"");
                intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) images);
                intent.putExtra("profile_name", person_name.getText().toString());
//                intent.putExtra("profile_thumbnail", new ConvertFile().UriToByteArray(getApplicationContext(), selectedImageUri));
                startActivity(intent);

            }

        }
        //프로필 사진 받아오는 코드
        else if (btn_choice == 1) {
            if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

                selectedImageUri = data.getData();
                profile.setImageURI(selectedImageUri);

            }
        }
    }


}


