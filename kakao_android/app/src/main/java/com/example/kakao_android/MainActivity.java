package com.example.kakao_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.exception.KakaoException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SessionCallback sessionCallback;
    private Context mContext;
    private KakaoUserModel model;
    private Button mLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        mLogoutButton = (Button)findViewById(R.id.kakao_logout_btn);

        // 임시로 일단 대충...? 구현해놓음. 필요 시 사용.
        mLogoutButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    protected void loginDoneActivity(){
        Intent intent = new Intent(this, LoginDoneActivity.class);
        intent.putExtra("Data", model);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback{

        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();
                    if(result == ApiErrorCode.CLIENT_ERROR_CODE){
                        Toast.makeText(mContext, "네트워크 연결 불안정. 잠시 후 다시 시도 바람.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(mContext, "로그인 도중 오류 발생 : " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                    super.onFailure(errorResult);
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(mContext, "세션 닫힘. 다시 시도: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    UserAccount kakaoAccount = result.getKakaoAccount();
                    String nickname = kakaoAccount.getProfile().getNickname();
                    String email = kakaoAccount.getEmail();
                    String gender = kakaoAccount.getGender().getValue();
                    String birthday = kakaoAccount.getBirthday();

                    model = new KakaoUserModel(nickname, email, gender, birthday);

                    loginDoneActivity();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Toast.makeText(mContext, "로그인 도중 오류 발생 인터넷 연결 확인 요함. : " + exception.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}