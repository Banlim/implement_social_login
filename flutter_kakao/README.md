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
  * debug용 keyhash 얻는 방법
  1. 새로운 프로젝트를 생성하거나, 파일 > 열기 > 현재 프로젝트 > android로 들어가서 열어서 MainActivity로 진입한다. 
    (꼭 MainActivity일 필요는 없다. 그냥 hash 로그 값을 확인하면 되기 때문)
  2. 해당 Activity에 아래의 함수를 넣어 run 한 후 hash 값 확인하여 kakao developer의 keyhash에 등록한다.
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
 
