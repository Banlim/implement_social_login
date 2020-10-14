package com.example.facebook_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginDoneActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private String mUsername;
    private String mEmail;

    private TextView mNameText;
    private TextView mEmailText;

    private Button mLogoutButton;
    private View.OnClickListener clickListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_done);
        init();
        getUser();
    }
    private void init(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mNameText = (TextView)findViewById(R.id.nickname_text);
        mEmailText = (TextView)findViewById(R.id.email_text);
        mLogoutButton = (Button)findViewById(R.id.facebook_logout_btn);
        clickListener = new BtnEventListener();
        mLogoutButton.setOnClickListener(clickListener);
    }

    private void getUser(){
        mUsername = mFirebaseUser.getDisplayName();
        mEmail = mFirebaseUser.getEmail();
        mNameText.setText(mUsername);
        mEmailText.setText(mEmail);
    }

    private class BtnEventListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.facebook_logout_btn){
                mFirebaseAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(LoginDoneActivity.this, SigninActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
