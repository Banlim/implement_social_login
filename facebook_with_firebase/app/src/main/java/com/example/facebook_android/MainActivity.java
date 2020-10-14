package com.example.facebook_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/*
    facebook의 경우 kakao와 마찬가지로 keyhash 값이 필요.
    그냥 kakao social login 구현할 때 쓴 keyhash 값 사용.
    Manifest 파일 && res/values/strings 파일의 App id와 login_protocol_scheme 수정.
 */

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 1500);

    }

    private class splashhandler implements Runnable {
        @Override
        public void run() {
            startActivity(new Intent(getApplication(), SigninActivity.class));
            finish();
        }
    }

}