# Facebook social login (with firebase)


### 추가할 사항

* Firebase에서 본인의 console을 생성한 후, google-services.json 파일을 해당 프로젝트의 app directory 아래에 복사한다.
* Facebook Developer에서 본인의 Application을 생성한 후, 빠른 시작 완료하기를 통해 app 설정을 완료한다.
* Firebase 본인 Console에서 Authentication > Sign-in method > Facebook을 클릭한 후, 그 안에 facebook app id와 secret code를 입력한다.
* Facebook Developer에서 본인의 Application > Facebook 로그인 > 설정 > client OAuth 설정 > Firebase에서 바로 윗 단계에서 얻은 Url을 입력한다.
* Facebook 로그인은 Kakao 로그인처럼 keyhash 값을 얻어야 사용할 수 있다.
  * 링크 참조 : [flutter_kakao login](https://github.com/Banlim/implement_social_login/blob/main/flutter_kakao/README.md)

* build.gradle(:project)
  ~~~gradle
  buildscript {
    repositories {
        . . .
        mavenCentral()
    }
    dependencies {
        . . .
        classpath 'com.google.gms:google-services:4.3.4'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
  }
  ~~~
  
* build.gradle(:app)
  ~~~gradle
  apply plugin: 'com.google.gms.google-services'
  dependencies {
    . . .
    implementation 'com.google.firebase:firebase-analytics:17.5.0'
    implementation 'com.google.firebase:firebase-auth:19.4.0'
    implementation 'com.facebook.android:facebook-android-sdk:7.1.0'
    implementation 'com.google.android.gms:play-services-auth:16.0.1'
  }
  ~~~
  
* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET" />
  
  <activity
    android:name="com.facebook.FacebookActivity"
    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
    android:label="@string/app_name" />

  <activity
    android:name="com.facebook.CustomTabActivity"
    android:exported="true">
    <intent-filter>
      <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="@string/fb_login_protocol_scheme" />
    </intent-filter>
  </activity>

  <meta-data
    android:name="com.facebook.sdk.ApplicationId"
    tools:replace="android:value"
    android:value="@string/facebook_app_id" />
  ~~~
  
* res/values/strings.xml
  ~~~xml
  <resources>
    <string name="facebook_app_id">{your app id}</string>
    <string name="fb_login_protocol_scheme">fb{your app id}</string>
  </resources>
  ~~~
  
### 주 내용

* Facebook 로그인 구현
  * activity_login.xml
  ~~~xml
  <com.facebook.login.widget.LoginButton
      android:id="@+id/facebook_login_btn"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_centerInParent="true"/>
  ~~~

  * init() 
    * CallbackManager를 통해 Callback을 등록한다.
    * FacebookLoginButton의 setPermission을 통해 여러가지 사용자 정보를 가져오는 것을 요청할 수 있다.
    * Permission의 종류는 내 앱 > 앱 검수 > 권한 및 기능에서 확인할 수 있다.
    * 필요한 권한을 Arrays.asList()에 추가하여 요청할 수 있다.
    * 단, Firebase에서는 이메일 및 이름 정도만 얻을 수 있으며, birthday, gender 등은 Firebase Auth에 저장이 되지 않기 때문에 Facebook 자체에서 얻어와야 한다.
    * 이를 가져오는 코드는 requestMe() 메소드에 구현되어있으며, onSuccess()일 때 호출하여 가져온다.
    * FacebookLoginButton의 registerCallback 메소드를 통해 CallbackManager와 LoginCallback을 버튼에 등록한다.
  ~~~java
  private void init(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mLoginCallback = new LoginCallback();

        // Facebook callback 등록
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mSigninFacebookButton = (LoginButton) findViewById(R.id.facebook_login_btn);
        
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
  ~~~
  
  * LoginCallback
  ~~~java
  private class LoginCallback implements FacebookCallback<LoginResult> {

      @Override
      public void onSuccess(LoginResult loginResult) {
          Log.d(TAG, "onSuccess");
          //requestMe(loginResult.getAccessToken());
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
  ~~~
  
  * handleFacebookAccessToken
    * 이 메소드를 통해 사용자가 로그인에 성공하면, callback method에서 로그인한 사용자의 AccessToken을 가져온다.
    * 이 AccessToken을 통해 Firebase 사용자 인증 정보로 교환 후 Firebase에 인증한다.
  ~~~java
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
  ~~~
  

### 참조한 링크
  * [Firebase로 Facebook 로그인](https://beomseok95.tistory.com/110)
  * [Android에서 Facebook 로그인을 사용하여 인증하기](https://firebase.google.com/docs/auth/android/facebook-login?hl=ko)
  * [안드로이드 Firebase 페이스북 로그인 예제](https://sh-itstory.tistory.com/61)
  * [Facebook 로그인 - Android와 Firebase의 연동](https://nittaku.tistory.com/16)
