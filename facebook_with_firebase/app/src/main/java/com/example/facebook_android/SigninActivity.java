package com.example.facebook_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SigninActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    LoginButton mSigninFacebookButton;
    CallbackManager mFacebookCallbackManager;
    LoginCallback mLoginCallback;

    private String TAG = SigninActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }
    private void init(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mLoginCallback = new LoginCallback();

        // Facebook callback 등록
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mSigninFacebookButton = (LoginButton) findViewById(R.id.facebook_login_btn);
        /*
           예를 들어 사용자의 birthday를 가져와야 하면, 아래 setPermission의 Arrays.asList에 "user_birthday" 추가 하는 식으로 해서 사용자 정보 가져올 수 있음.
           ## facebook의 경우 birthday가 생년월일 다 가져옴.
           권한 종류는 Facebook Developer의 내 앱 > 앱 검수 > 권한 및 기능에서 확인할 수 있음.
           단, Firebase에서는 이메일, 이름 정도만 얻을 수 있고, birthday, gender 등은 Firebase Auth에 저장?이 되지 않는 듯함.
           따라서, 이 외의 정보를 가져오기 위해서는 Facebook 쪽에서 GraphRequest를 사용하여 가져와야함.
           가져오는 코드는 requestMe() 함수에 정의되어 있으며, LoginCallback의 onSuccess()일 때 호출.
         */
//        mSigninFacebookButton.setPermissions(Arrays.asList("public_profile", "email", "user_gender", "user_birthday"));
        mSigninFacebookButton.setPermissions(Arrays.asList("public_profile", "email"));
        mSigninFacebookButton.registerCallback(mFacebookCallbackManager, mLoginCallback);

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Log.d(TAG, "sign in");
                    loginDoneActivity();
                } else {
                    Log.d(TAG, "sign out");
                }
            }
        };
    }
    /*
    public void requestMe(AccessToken token){
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("RESULT::", object.toString());
                try {
                    String gender = object.getString("gender");
                    String birthday = object.getString("birthday");
                    Log.d("GENDER : ", gender);
                    Log.d("BIRTHDAY : ", birthday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        Log.d(TAG, parameters.getString("fields"));
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }
     */


    private void loginDoneActivity(){
        Intent intent = new Intent(SigninActivity.this, LoginDoneActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFirebaseAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 사용자가 로그인에 성공하면, 페이스북 로그인 버튼의 callback method에서 로그인한 사용자의 AccessToken 가져옴.
    // 이 AccessToken을 통해 Firebase 사용자 인증 정보로 교환 후 Firebase에 인증.
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete()) {
                    Toast.makeText(SigninActivity.this, "login Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SigninActivity.this, "login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class LoginCallback implements FacebookCallback<LoginResult> {

        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "onSuccess");
//            requestMe(loginResult.getAccessToken());
            handleFacebookAccessToken(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "OnCancel");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG, "OnError");
        }
    }
}
