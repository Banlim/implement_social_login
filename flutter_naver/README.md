# flutter_naver_login

A new Flutter application.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://flutter.dev/docs/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://flutter.dev/docs/cookbook)

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.


### 추가할 사항

* build.gradle(:app)
  ~~~gradle
  // 19 이상이면 된다.
  minSdkVersion 19
  ~~~
  
* pubspec.yaml
  ~~~yaml
  dependencies:
    flutter_naver_login: ^1.2.0
    http: ^0.12.0+1
  ~~~
  
* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET" />

  <application
    . . .
    <meta-data
        android:name="com.naver.sdk.clientId"
        android:value="@string/client_id" />
    <meta-data
        android:name="com.naver.sdk.clientSecret"
        android:value="@string/client_secret" />
    <meta-data
        android:name="com.naver.sdk.clientName"
        android:value="@string/client_name" />
  </application>
  ~~~
  
* res/values/strings.xml
  ~~~xml
  <resources>
    <string name="client_id">{your native app id}</string>
    <string name="client_secret">{your native app secret}</string>
    <string name="client_name">{your native app name}</string>
  </resources>
  ~~~
  
  
### 주 내용

* _loginWithNaver()
  ~~~dart
  Future<void> _loginWithNaver() async {
   await FlutterNaverLogin.logIn().then((value){
     setState(() {
       name = value.account.name;
       isLogin = true;
     });
   }).catchError((e){
   });
   await _accessTokenWithNaver();
  }
  ~~~
  
* _accessTokenWithNaver()
  ~~~dart
  Future<void> _accessTokenWithNaver() async {
    await FlutterNaverLogin.currentAccessToken.then((value) {
      setState(() {
        accessToken = value.accessToken;
        tokenType = value.tokenType;
      });
      _getUser();
    }).catchError((e){
      print('access token error : ${e}');
    });
  }
  ~~~
  
* _getUser()
  ~~~dart
  Future<void> _getUser() async {
    NaverAccountResult res = await FlutterNaverLogin.currentAccount();
    setState(() {
      nickName = res.nickname;
      email = res.email;
      gender = res.gender;
      birthday = res.birthday;
    });
  }
  ~~~
  
* _logoutWithNaver()
  * flutter에서 naver logout의 자동으로 로그아웃이 되는 것이 아니라 사용자가 직접 로그아웃하기를 눌러야 한다.
  * 아래 코드는 연동을 해제하는 코드인 것 같다.
  ~~~dart
  Future<void> _logoutWithNaver() async{
    await FlutterNaverLogin.logOut().then((value) {
      setState(() {
        nickName = "nickName";
        email = "email";
        gender = "gender";
        birthday = "birthday";
      });
    });
    await http.get(Uri.encodeFull('http://nid.naver.com/nidlogin.logout')).then((value) {
      print('logout');
    });
  }
  
  
### 참조한 링크
* [flutter_naver_login](https://github.com/yoonjaepark/flutter_naver_login?fbclid=IwAR3ZlZEbL88F2KVRCcYBpIdsGOcSJLBRorBUOOgXNC8qi_SdvVx7wHl7ft0)
* [flutter_naver_login (pub.dev)](https://pub.dev/packages/flutter_naver_login)
