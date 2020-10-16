package com.example.google_android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private String TAG = MainActivity.class.getName();

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 사용자의 ID 및 기본 프로필 정보를 요청하려면 GoogleSignInOptions에 DEFAULT_SIGN_IN 매개 변수를 사용하여 build한다.
        // 이 때 사용자의 Email이 필요하다면, requestEmail() 옵션을 추가하여 요청할 수 있다.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        setSignInButton();
    }

    private void setSignInButton(){
        SignInButton signInButton = findViewById(R.id.google_login);
        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);

        signInButton.setOnClickListener((view) -> onClick(view));
    }

    // 사용자가 구글로 로그인하기 버튼을 클릭하면 아래와 같은 signIn() 함수가 실행된다.
    // googleSignInClient.getSignInIntent() 함수를 호출하여 intent를 얻고, 이 intent를 startActivityForResult 함수를 통해 실행한다.
    private void signIn(){
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // 사용자가 로그인에 성공하면 사용자의 GoogleSignInAccount object를 얻을 수 있다.
    // account에는 로그인한 사용자의 이름과 같은 정보가 담겨있다.
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void onClick(View v){
        switch (v.getId()){
            case R.id.google_login:
                Log.d(TAG, "click login button");
                signIn();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 사용자가 이미 구글로 앱에 로그인 했는지 확인하는 코드이다.
        // 사용자가 이미 구글로 앱에 로그인 한 적이 있다면, GoogleSignInAccount object를 return한다.
        // 사용자가 구글로 로그인한 적이 없다면, account = null이 된다.
        // 따라서 null 값 유무에 따라 updateUI 함수를 구성하면 좋을 것 같다.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account){
        if(account != null){
            String name = account.getDisplayName();
            String email = account.getEmail();
            String id = account.getId();
            Log.d(TAG, "name : " + name);
            Log.d(TAG, "email : " + email);
            Log.d(TAG, "id : " + id);
        }
    }


}