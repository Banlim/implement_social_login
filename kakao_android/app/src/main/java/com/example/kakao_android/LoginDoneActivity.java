package com.example.kakao_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginDoneActivity extends AppCompatActivity {
    private TextView nickName;
    private TextView email;
    private TextView gender;
    private TextView birthday;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_done);
        init();
        Intent intent = getIntent();
        if(intent != null){
            KakaoUserModel model;
            model = (KakaoUserModel) intent.getSerializableExtra("Data");
            nickName.setText("nickName : " + model.getNickname());
            email.setText("email : " + model.getEmail());
            gender.setText("gender : " + model.getGender());
            birthday.setText("birthday : " + model.getBirthday());
        }
    }

    private void init(){
        nickName = findViewById(R.id.login_done_nickname);
        email = findViewById(R.id.login_done_email);
        gender = findViewById(R.id.login_done_gender);
        birthday = findViewById(R.id.login_done_birthday);
    }
}
