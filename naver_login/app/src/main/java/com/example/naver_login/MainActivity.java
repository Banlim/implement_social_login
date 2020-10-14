package com.example.naver_login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MAIN_ACTIVITY";
    private String OAUTH_CLIENT_ID;
    private String OAUTH_CLIENT_SECRET;
    private String OAUTH_CLIENT_NAME;

    private static OAuthLogin mOAuthLoginInstance;
    private OAuthLoginButton mOAuthLoginButton;
    private Context mContext;
    private NaverUserModel model;

    /* temp button
    logout && 연동 해제 버튼 (필요하면 사용?)
     */
    private Button mLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        init();
    }

    private void init(){
        OAUTH_CLIENT_ID = mContext.getString(R.string.client_id);
        OAUTH_CLIENT_SECRET = mContext.getString(R.string.client_secret);
        OAUTH_CLIENT_NAME = mContext.getString(R.string.client_name);

        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.naver_login_btn);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);

        mLogoutButton = (Button)findViewById(R.id.naver_logout_btn);

        /* *********************************************************************
        임시로 로그아웃 (&& 연동 해제) 테스트
         */
        mLogoutButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // logout만 하는 코드
                mOAuthLoginInstance.logout(mContext);
                // logout && 연동 해제까지 하는 코드
//                new RemoveTokenTask().excute("logout");
            }
        });
    }
    /*
    ******************************************************************************
     */

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                Log.d(TAG, "success : " + accessToken);
                Log.d(TAG, "expiresAt : " + Long.toString(expiresAt));
                getUser(accessToken);
            }
            else{
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Log.d(TAG, "errorCode : " + errorCode);
                Log.d(TAG, "errorDesc : " + errorDesc);
            }
        }
    };

    protected void loginDoneActivity(){
        Intent intent = new Intent(this, LoginDoneActivity.class);
        intent.putExtra("Data", model);
        startActivity(intent);
//        finish();
    }

    private void getUser(String token){
        new GetUserTask().excute(token);
    }

    /*
    User Profile 조회
    AsyncTask 사용하려 했으나, API 30부터 지원이 중단되어 따로 class 생성하여 구현.
     */

    private class GetUserTask extends ThreadTask<String, String>{
        @Override
        protected String doInBackground(String s) {
            String header = "Bearer " + s;
            String url = "https://openapi.naver.com/v1/nid/me";

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", header);
            String responseBody = get(url, requestHeaders);

            return responseBody;
        }

        private String get(String url, Map<String, String> requestHeaders){
            HttpURLConnection connection = connect(url);
            try {
                connection.setRequestMethod("GET");
                for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return readBody(connection.getInputStream());
                } else {
                    return readBody(connection.getErrorStream());
                }
            } catch (IOException e) {
                throw new RuntimeException("API 요청 및 응답 실패");
            } finally {
                connection.disconnect();
            }
        }

        private HttpURLConnection connect(String apiurl){
            try{
                URL url = new URL(apiurl);
                return (HttpURLConnection)url.openConnection();
            } catch (MalformedURLException e) {
                throw new RuntimeException("API URL이 잘못되었습니다. : " + apiurl, e);
            } catch (IOException e) {
                throw new RuntimeException("연결을 실패했습니다. : " + apiurl, e);
            }
        }

        private String readBody(InputStream body){
            InputStreamReader streamReader = new InputStreamReader(body);
            try(BufferedReader lineReader = new BufferedReader(streamReader)){
                StringBuilder responseBody = new StringBuilder();
                String line;
                while((line = lineReader.readLine()) != null){
                    responseBody.append(line);
                }
                return responseBody.toString();
            } catch (IOException e) {
                throw new RuntimeException("API 응답을 읽는데 실패했습니다. ", e);
            }
        }

        /*
        nickname, email, gender, birthday 외에도 회원 이름(본명?), 프로필 사진, 연령대도 가져올 수 있음.
         */
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                Log.d("TAG ", s);
                if(jsonObject.getString("resultcode").equals("00")){
                    JSONObject object = new JSONObject(jsonObject.getString("response"));
                    String nickname = object.getString("nickname");
                    String email = object.getString("email");
                    String gender = object.getString("gender");
                    String birthday = object.getString("birthday");
                    model = new NaverUserModel(nickname, email, gender, birthday);
                }
                loginDoneActivity();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    임시로 만든 class (필요 시 Token 삭제를 위해)
     */

    private class RemoveTokenTask extends ThreadTask<String, Void> {

        @Override
        protected Void doInBackground(String arg) {
            boolean isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(mContext);

            if(!isSuccessDeleteToken){
                Log.d("REMOVE", "errorCode : " + mOAuthLoginInstance.getLastErrorCode(mContext));
                Log.d("REMOVE", "errorDesc : " + mOAuthLoginInstance.getLastErrorDesc(mContext));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Result) {
            Toast.makeText(mContext, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
