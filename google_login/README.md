# Google social login without firebase


### 추가할 사항

* [Google API console](https://console.developers.google.com/apis/credentials?project=_)에서 프로젝트 생성
* API 및 서비스 > 사용자 인증 정보 > OAuth 2.0 Client ID 생성
* OAuth 2.0 Client ID를 생성하기 위해선 OAuth 동의 화면으로 넘어가서 설정.
* OAuth 2.0 Client ID 만들 때 SHA-1 인증서 디지털 지문이 필요.
* SHA-1 인증서 
  * Android studio에서 우측에 있는 gradle tab 클릭 
  * your project name > app > Tasks > android > signingReport 클릭
  * Log로 SHA-1 인증서 복사 후 붙여넣기.

* build.gradle(:app)
  ~~~gradle
  android {
      . . . 
      compileOptions{
          sourceCompatibility JavaVersion.VERSION_1_8
          targetCompatibility JavaVersion.VERSION_1_8
      }
  }

  dependencies {
      implementation 'com.google.android.gms:play-services-auth:18.1.0'
  }
  ~~~

* build.gradle(:project)
  ~~~gradle
  buildscript {
    repositories {
        // google() 있는지 check
        google()
        jcenter()
    }
  }

  allprojects {
      repositories {
        // google() 있는지 check
        google()
        jcenter()
      }
  }
  ~~~
  
* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET"/>
  ~~~
  
  
### 주 내용

* google login button
  ~~~xml
  <com.google.android.gms.common.SignInButton
        android:id="@+id/google_login"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"/>
  ~~~
  
  * login button set size :: SIZE_STANDARD, SIZE_WIDE, SIZE_ICON_ONLY
  ~~~java
  SignInButton signInButton = findViewById(R.id.google_login);
  signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
  ~~~

* GoogleSignInOptions
  * 사용자의 ID 및 기본 프로필 정보를 요청하려면 GoogleSignInOptions에 DEFAULT_SIGN_IN 매개 변수를 사용하여 build한다.
  * 이 때 사용자의 Email이 필요하다면, requestEmail() 옵션을 추가하여 요청할 수 있다.
  ~~~java
  GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
  ~~~
  
* GoogleSignInClient
  * GoogleSignInClient를 gso로 지정한 옵션으로 build한다.
  ~~~java
  mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
  ~~~
  
* GoogleSignIn.getLastSignedInAccount
  * 사용자가 이미 구글로 앱에 로그인 했는지 확인하는 코드를 override한 onStart 함수 내에 작성한다.
  * 사용자가 이미 구글로 앱에 로그인한 적이 있다면, GoogleSignInAccount object를 return한다.
  * 사용자가 구글로 로그인한 적이 없다면, account = null이 된다.
  * 따라서 account가 null인지 유무에 따라 updateUI 함수를 구성할 수 있다.
  ~~~java
  protected void onStart() {
     super.onStart();
     GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
     updateUI(account);
  }
  ~~~
  
* click login button
  * 사용자가 구글로 로그인하기 버튼을 클릭하면 아래와 같은 signIn() 함수가 실행된다.
  * googleSignInIntent() 함수를 호출하여 intent를 얻고, 이 intent를 startActivityForResult 함수를 통해 실행한다.
  ~~~java
  private void signIn(){
      Intent intent = mGoogleSignInClient.getSignInIntent();
      startActivityForResult(intent, RC_SIGN_IN);
  }
  ~~~
  
* GoogleSignInAccount
  * 사용자가 로그인에 성공하면 사용자의 GoogleSignInAccount object를 얻을 수 있다.
  * account에는 로그인한 사용자의 이름과 같은 정보가 담겨있다.
  ~~~java
  private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
    try {
        GoogleSignInAccount account = completedTask.getResult(ApiException.class);
        updateUI(account);
    } catch (ApiException e) {
        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        updateUI(null);
    }
  }
  ~~~
  
* updateUI
  ~~~java
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
  ~~~
  
* 추후에 사용자의 birthday, gender와 같은 정보도 요청하고 가져오는 코드도 추가할 예정
  
### 참조한 링크
* [Google Sign-in for Android guide](https://developers.google.com/identity/sign-in/android/sign-in)
* [Google play services version check](https://developers.google.com/android/guides/setup)
* [Android Google Sign in Example](https://m.blog.naver.com/PostView.nhn?blogId=nakim02&logNo=221409188247&proxyReferer=https:%2F%2Fwww.google.com%2F)
