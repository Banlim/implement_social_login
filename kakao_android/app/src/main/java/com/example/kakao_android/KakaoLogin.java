package com.example.kakao_android;

import android.app.Application;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

public class KakaoLogin extends Application {
    private static volatile KakaoLogin instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        KakaoSDK.init(new KakaoSDKAdapter());
    }
    public static KakaoLogin getKakaoApplicationContext(){
        if(instance == null){
            throw new IllegalStateException("this application does not inherit com.example.kakao_login");
        }
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }
}
