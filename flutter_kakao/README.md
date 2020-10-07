# flutter_kakao_login

A new Flutter application.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://flutter.dev/docs/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://flutter.dev/docs/cookbook)

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## 추가할 사항

* build.gradle (project)
  * minSDKversion: 19 (이상)
  
* pubspec.yaml
  ~~~dart
  dependencies:
    flutter:
      sdk: flutter
    kakao_flutter_sdk: ^0.4.2
  ~~~

* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET" />
  
  <activity android:name="com.kakao.sdk.flutter.AuthCodeCustomTabsActivity">
    <intent-filter android:label="flutter_web_auth"> <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:scheme="kakao{your app key}" android:host="oauth"/>
    </intent-filter>
  </activity>
  
  <meta-data
    android:name="com.kakao.sdk.AppKey"
    android:value="{your app key}" />
  ~~~
  
* packaga name이 kakao developer application의 패키지명과 일치하는지 확인
 
* debug용 keyhash는 keytool을 사용하여 얻으면 로그인 동작X
  * debug용 keyhash 얻는 방법</br>
  
   1. 새로운 프로젝트를 생성하거나, 파일 > 열기 > 현재 프로젝트 > android로 들어가서 열어서 MainActivity로 진입한다. </br>
   (꼭 MainActivity일 필요는 없다. 그냥 hash 로그 값을 확인하면 되기 때문)
   2. 해당 Activity에 아래의 함수를 넣어 run 한 후 hash 값 확인하여 kakao developer의 keyhash에 등록한다.
   3. keyhash를 등록한 후, 아래 코드는 딱히 필요가 없으니 지워도 무방하다.
  
  ~~~java
      private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;

                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                System.out.println("HASH  " + something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {

            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }
    ~~~


## 주 내용

* kakao client ID 선언
  * 코드 아무데나 (나의 경우 main에 넣었다.) KakaoContext.clientId 값을 넣어준다.
  * clientId 값은 native key를 넣어주어야 한다.
  ~~~dart
  KakaoContext.clientId = 'your native app key';
  ~~~
  
* _initKakaoTalkInstalled()
  * 위 함수를 통해 카카오톡이 현재 기기에 설치되어있는지 boolean 값으로 리턴한다.
  * 카카오톡이 설치되어있으면 카카오톡을 통한 로그인이 진행되며, 설치되어있지 않으면 web view를 띄워 로그인하도록 구현했다.

* authCode
  * AuthCodeClient.instance.request() 를 통해 카카오 서버에 인증 코드를 달라고 요청한다.
  * 카카오 서버는 위 요청을 받으면, 사용자가 필수 항목에 동의하고 로그인을 요청했는지 확인 후, 인증 코드를 발급한다.
  * 이 인증 코드을 기반으로 사용자 토큰을 요청할 수 있다.

* issueAccessToken
  * AuthApi.instance.issueAccessToken(authCode) 를 통해 이전에 발급 받은 인증 코드를 기반으로 토큰을 요청한다.
  * 이 AccessToken은 사용자를 인증하고 카카오 Api 호출 권한을 부여한다.
  * 로그인을 통해 앱과 사용자가 연결되고, 토큰도 발급 받으면 사용자 정보 요청을 통해 현재 로그인한 사용자 정보를 발급받을 수 있다.
  
* getUser()
  * UserApi.instance.me() 를 통해 사용자 정보를 가져올 수 있다.
  * 사용자가 동의한 항목에 대해서만 가져올 수 있으며, 아래 코드와 같이 kakaoAccount를 통해 접근하여 받을 수 있다.
  * 해당 프로젝트의 경우, KakaoUserModel 라는 일종의 Model class를 생성하여 관리하도록 설계하였다.
  ~~~dart
  await UserApi.instance.me()
      .then((user) {
    setState(() {
      String nickname = user.kakaoAccount.profile.toJson()['nickname'].toString();
      String email = user.kakaoAccount.email.toString();
      String gender = user.kakaoAccount.gender.toString();
      String birthday = user.kakaoAccount.birthday.toString();
      userModel = KakaoUserModel(nickname, email, gender, birthday, '1998', '01012345678');
    });
  });
  ~~~
