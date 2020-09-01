package com.example.fade;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class Gallery extends AppCompatActivity {
    static final int REQ_CODE_SELECT_IMAGE = 42;
    static final int SELECT_IMAGE = 100;
    CircleImageView profile;
    Uri selectedImageUri;
    int btn_choice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Button personAdd = (Button) findViewById(R.id.btn_personAdd);
        Button profileAdd = (Button) findViewById(R.id.btn_profileAdd);
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
                ImagePicker.create(Gallery.this).start();
            }
        });
    }

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


                Intent intent = new Intent(Gallery.this, Gallery2.class);
                intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) images);
                startActivity(intent);

            }

        }
        else if (btn_choice == 1) {
            if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

                selectedImageUri = data.getData();
                profile.setImageURI(selectedImageUri);
            }
        }
    }
}