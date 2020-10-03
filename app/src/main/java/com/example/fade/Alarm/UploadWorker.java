package com.example.fade.Alarm;

import android.content.Context;
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
import java.util.ArrayList;

public class UploadWorker extends Worker {
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                // 사용하고자 하는 코드
                Toast.makeText(getApplicationContext(), "므엥", Toast.LENGTH_SHORT).show();
                Log.e("므엥 성공", "tq");


            }
        });
//
//        Thread t = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    //갤러리 이미지 가져오기
//
//                    GalleryUpdate galleryUpdate = new GalleryUpdate(getApplicationContext());
//
//                    ArrayList<byte[]> byteList = galleryUpdate.getByteArrayOfRecentlyImages();
//                    CommServer commServer = new CommServer(getApplicationContext());
//                    Log.i("updateGalleryImg","실행 시작");
//                    //서버에 보낸 후 값 받기
//                    commServer.updateGalleryImg(byteList, galleryUpdate.groupUriList);//갤러리 경로변경할 이미지의 uri 리스트 따로 받아옴
//
//
//                }catch (IOException e){
//                    Log.i("updateGalleryImg ", e.getMessage());
//
//                }
//            }
//        };
//        t.start();
        Log.e("dowork으로 들어옴", "tq");
        AlarmReceiver alarmReceiver = new AlarmReceiver();

        alarmReceiver.createNotificationChannel(getApplicationContext());
        Log.e("dowork 완료", "tq");

        return Result.success();
    }

}
