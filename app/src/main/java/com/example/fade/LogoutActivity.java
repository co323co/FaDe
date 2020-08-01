package com.example.fade;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class LogoutActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);


        Toast.makeText(getApplicationContext(),getIntent().getExtras().getString("setting_name"), Toast.LENGTH_SHORT).show();


        if(getIntent().getExtras().getString("setting_name")=="로그아웃"){
            Log.e("로그아웃으로 들어왔음", "");

            /*mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"로그아웃 완료",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });*/

        }
        if(getIntent().getExtras().getString("setting_name")=="계정 데이터 삭제"){
            Toast.makeText(getApplicationContext(),getIntent().getExtras().getString("setting_name"), Toast.LENGTH_SHORT).show();


            /*
            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"계정 데이터 삭제 완료",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });*/
        }



    }

}
