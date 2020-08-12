package com.example.fade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    public static String UserID = null;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 900;
    GoogleSignInAccount userAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 사용자 ID, 이메일 주소 및 기본을 요청하도록 로그인 구성
        // 프로필. ID 및 기본 프로필은 DEFAULT_SIGN_IN에 포함되어 있습니다.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); //gso 옵션으로 intent 생성->startActivityForResult()에 인수로 전달하여 실행
                                                                            //startActivityForResult()는 intent에 입력된 액티비티로부터 결과를 받을 때 이용하는 메소드
                                                                            //사용자가 startActivityForResult()로 호출된 액티비티 작업 다 끝내면 onActivityResult()실행된다.

                 //로그인 하면 바로 버튼 2개 화면으로 넘어가기
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this); //기존에 로그인 된 사용자 객체 얻기
        if(account!=null){
            userAccount = account;
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);      //null이 아닌 경우 이 사용자는 이미 구글 로그인 된 상태, null 일 경우 로그인 한 적 없음
            startActivity(intent);
            profile();
            finish();

        }


        setContentView(R.layout.activity_login);
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();

            }
        });

        //임시버튼



        //임시버튼, 프로필 테스트용


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN); // 구글 로그인 창, 구글 계정 고르기(사용자에게 허가 요청하는 액티비티 띄움, 사용자의 행동 입력받음)

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//signIn()호출 후, onActivityResult() 호출, 사용자가 액티비티에서 한 행동 data로 받아옴
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN){//이 시점에서 이미 로그인이 되어 있다.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data); //tast<>객체로 변환
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class); //getResult()로 GoogleSignInAccount 객체 반환
            userAccount = account; //로그인된 계정 정보

            //로그인 후 화면 전환
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            //intent.putExtra("id", userAccount.getId());
            startActivity(intent);
            //profile();  //사용자 정보 토스트로 출력

            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG,"signInResult:failed code=" + e.getStatusCode());
        }
    }
    private void profile(){
        String email = userAccount.getEmail();
        String id = userAccount.getId();
        String familyname = userAccount.getFamilyName();
        String givenname = userAccount.getGivenName();
        UserID = userAccount.getId();

        Toast.makeText(getApplicationContext(),"email : " + email + "\nid = " + id + "\nfamilyname = " + familyname + "\ngivenname = " + givenname, Toast.LENGTH_SHORT).show();
    }
    private void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void revokeAccess(){
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

}