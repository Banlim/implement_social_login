# naver social login with custom thread


### 추가할 사항
* [네이버 아이디로 로그인 sdk 다운로드](https://github.com/naver/naveridlogin-sdk-android/releases)
  * 다운로드 후, Android studio의 File > New > New Module > Import .JAR/.AAR Package > sdk 다운 받은 경로 입력 후 Finish
  * File > Project Structure > Dependencies > app 폴더 선택 > Dependency 여러 개 있는 부분에서 + 버튼 클릭 > Module dependency > naver sdk~~ 써있는 거 클릭 후 apply

* build.gradle(:app)
  ~~~gradle
  dependencies {
    implementation project(path: ':naveridlogin_android_sdk_4.2.6')
  }
  ~~~
  
* proguard-rules.pro
  ~~~pro
  -keep public class com.nhn.android.naverlogin.*{
    public protected *;
  }
  ~~~
  
* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET"/>
  ~~~
  
* res/values/strings.xml
  ~~~xml
  <resources>
    <string name="client_id">{your client id}</string>
    <string name="client_secret">{your client secret}</string>
    <string name="client_name">{your client name (아무거나 상관 없음)}</string>
  </resources>
  ~~~
  
  
### 주 내용
  
* 로그인 버튼 구현
  ~~~xml
  <com.nhn.android.naverlogin.ui.view.OAuthLoginButton
        android:id="@+id/naver_login_btn"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:src="@mipmap/ic_naver_login_round"
        android:layout_centerInParent="true"/>
  ~~~
  
* initialize
  ~~~java
  private void init(){
        OAUTH_CLIENT_ID = mContext.getString(R.string.client_id);
        OAUTH_CLIENT_SECRET = mContext.getString(R.string.client_secret);
        OAUTH_CLIENT_NAME = mContext.getString(R.string.client_name);

        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.naver_login_btn);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
  }
  
* login handler 적용
  ~~~java
  private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                getUser(accessToken);
            }
            else{
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
            }
        }
    };
  ~~~
  
* User Profile 조회
  * API 30 부터 AsyncTask 지원을 중단하는 관계로 ThreadTask라는 class를 생성하여 AsyncTask와 유사하게 동작하도록 구성.
  ~~~java
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
  ~~~
  
* User Task Thread
  ~~~java
  private void getUser(String token){
        new GetUserTask().excute(token);
  }
  ~~~
  
* ThreadTask.java
  * ThreadTask는 [이 링크](https://calvinjmkim.tistory.com/35)를 참조하여 필요한 부분만 구현
  ~~~java
  public abstract class ThreadTask<T1, T2> implements Runnable {
    T1 mArgument;
    T2 mResult;

    final public void excute(final T1 arg){
        mArgument = arg;
        Thread thread = new Thread(this);
        thread.start();

        try {
            thread.join();
        }
        catch (InterruptedException e){
            e.printStackTrace();
            onPostExecute(null);
            return;
        }
        onPostExecute(mResult);
    }

    @Override
    public void run() {
        mResult = doInBackground(mArgument);
    }

    protected abstract T2 doInBackground(T1 arg);

    protected abstract void onPostExecute(T2 Result);
  }
  ~~~
  
