# Native_kakao_login


### 추가할 사항
  
* build.gradle(:app)
~~~gradle
    implementation group: project.KAKAO_SDK_GROUP, name: 'usermgmt', version: project.KAKAO_SDK_VERSION
~~~

* build.gradle(:project)
~~~gradle
allprojects {
    repositories {
        // 아래 두 줄 추가
        mavenCentral()
        maven{ url 'http://devrepo.kakao.com:8088/nexus/content/groups/public/'}
    }
}
~~~

* gradle.properties
~~~properties
KAKAO_SDK_GROUP=com.kakao.sdk
KAKAO_SDK_VERSION=1.27.0
~~~

* AndroidManifest.xml
~~~xml
<uses-permission android:name="android.permission.INTERNET" />

<activity android:name="com.kakao.sdk.auth.AuthCodeHandlerActivity">
    <intent-filter>
      <action android:name="android.intent.action.VIEW"/>
      <category android:name="android.intent.category.DEFAULT"/>
      <category android:name="android.intent.category.BROWSABLE"/>
      <data android:host="oauth"
            android:scheme="kakao{your native app key}"/>
    </intent-filter>
</activity>

<meta-data
   android:name="com.kakao.sdk.AppKey"
   android:value="{your native app key}" />
~~~

* debug keyhash
  * 링크 참조 : [flutter_kakao login](https://github.com/Banlim/implement_social_login/blob/main/flutter_kakao/README.md)
  
### 주 내용

* 프로젝트와 kakao sdk 연결
  1. kakao sdk를 사용하기 위해서는 우선, 초기화가 필요하다.
  2. 따라서, onCreate() 함수 내에 Kakao.init(new KakaoSDKAdapter()); 라인을 작성한다.
  3. 이후 AndroidManifest.xml 파일의 application의 이름을 KakaoLogin으로 설정한다. (Application을 상속한 class 이름이면 된다.)
  
  * KakaoLogin.java
  ~~~java
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
  ~~~
  
  * AndroidManifest.xml
  ~~~xml
    <application
        android:name=".KakaoLogin"
        . . .
        >
    </application>
  ~~~
  
* KakaoSDKAdapter 작성
  ~~~java
  public class KakaoSDKAdapter extends KakaoAdapter {

    // 로그인 시 사용 될, Session의 옵션 설정을 위한 인터페이스
    // Session은 로그인 객체를 유지시키는 객체로 Access Token을 관리하는 역할.
    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
            }

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Nullable
            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return true;
            }
        };
    }
    
    // Application이 가지고 있는 정보를 얻기 위한 인터페이스.
    @Override
    public IApplicationConfig getApplicationConfig() {
        return new IApplicationConfig() {
            @Override
            public Context getApplicationContext() {
                return KakaoLogin.getKakaoApplicationContext();
            }
        };
    }
  }
  ~~~
  
* Session Callback class 구현
  ~~~java
      private class SessionCallback implements ISessionCallback{
      
        // 로그인 성공 시
        @Override
        public void onSessionOpened() {
            // 이전에는 UserManagement.requestMe() 였으나, 현재는 UserManagement.getInstance().me() 함수로 사용자 정보 요청 가능
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
            
                // 사용자 정보 요청 실패
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
                
                // Session이 닫힌 경우
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(mContext, "세션 닫힘. 다시 시도: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                
                // 사용자 정보 요청 성공
                @Override
                public void onSuccess(MeV2Response result) {
                    // result.getKakaoAccount() 함수를 통해 UserAccount를 받아옴.
                    // 이전에는 result가 UserProfile 형태로 왔으나 현재는 MeVResponse 타입으로 온다.
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
        
        // 로그인 실패
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Toast.makeText(mContext, "로그인 도중 오류 발생 인터넷 연결 확인 요함. : " + exception.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    ~~~
    
* Logout 구현
  * UserManagement에서 requestLogout을 호출하여 구현할 수 있다.
  ~~~java
  UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
    @Override
      public void onCompleteLogout() {
        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
      }
  });
  ~~~
  
  * 연동 해제 코드
  ~~~java
  UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
    @Override
      public void onSessionClosed(ErrorResult errorResult) {
        Log.d(TAG, "kakao session X");
      }

    @Override
      public void onSuccess(Long result) {
        Log.d(TAG, "카카오 연결 해제 성공");
      }
  });
  ~~~
