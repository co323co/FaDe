package com.example.fade.Alarm;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import androidx.core.app.NotificationCompat;

import com.example.fade.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {
    // Channel에 대한 id 생성
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    // Channel을 생성 및 전달해 줄 수 있는 Manager 생성
    private NotificationManager mNotificationManager;

    // Notification에 대한 ID 생성
    private static final int NOTIFICATION_ID = 0;

    // Notification을 호출할 button 변수
    private Button button_notify;

    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        //notification manager 생성

        createNotificationChannel(context);

        sendNotification(context);

        //}


    }
    //채널을 만드는 메소드
    public NotificationCompat.Builder createNotificationChannel(final Context context)
    {

        mNotificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(context);
        // Manager를 통해 notification 디바이스로 전달


        if(Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID
                    ,"Test Notification",mNotificationManager.IMPORTANCE_HIGH);
            //Channel에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            // Manager을 이용하여 Channel 생성

            if (mNotificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                mNotificationManager.createNotificationChannel(notificationChannel);
            }

        }else notifyBuilder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        return notifyBuilder;

    }

    // Notification Builder를 만드는 메소드
    private NotificationCompat.Builder getNotificationBuilder(Context context) {
        BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.logo);
        Bitmap bitmap = drawable.getBitmap();



        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"image/*");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("그룹 별 갤러리 자동정리가 완료되었습니다!")
                .setContentIntent(pendingIntent)
                .setContentText("갤러리로 들어가서 확인해보세요!");

        if (mNotificationManager != null) {

            // 노티피케이션 동작시킴
            mNotificationManager.notify(1234, notifyBuilder.build());
        }


        return notifyBuilder;
    }

    // Notification을 보내는 메소드
    public void sendNotification(Context context){
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(context);
        // Manager를 통해 notification 디바이스로 전달
        mNotificationManager.notify(NOTIFICATION_ID,notifyBuilder.build());
    }
}