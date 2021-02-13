package com.example.fade.Alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fade.GalleryUpdate;
import com.example.fade.R;
import com.example.fade.Server.CommServer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class UploadWorker extends Worker {
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    //갤러리 이미지 가져오기
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm");

                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("alarm_check", MODE_PRIVATE);
                    String last_update = sharedPrefs.getString("last_update", dateFormat.format(new Date()));
                    Log.e("마지막 업뎃 날짜", last_update);

                    ArrayList<Uri> groupUriList = new ArrayList<>();


                    GalleryUpdate galleryUpdate = new GalleryUpdate(getApplicationContext(), groupUriList, last_update);

                    ArrayList<byte[]> byteList = galleryUpdate.getByteArrayOfRecentlyImages();
                    CommServer commServer = new CommServer(getApplicationContext());

                    //서버에 보낸 후 값 받기
                    commServer.updateGalleryImg(byteList, galleryUpdate.groupUriList);//갤러리 경로변경할 이미지의 uri 리스트 따로 받아옴
                }catch (IOException e){
                    Log.i("updateGalleryImg ", e.getMessage());

                }
            }
        };
        t.start();


        return Result.success();
    }

}
