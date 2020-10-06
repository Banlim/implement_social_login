import 'package:flutter/material.dart';
import 'package:kakao_flutter_sdk/all.dart';

import 'Login/kakao_login.dart';

/*
build.gradle (project) --> minSDKversion: 19(이상)
pubspec.yaml (dependencies) --> kakao_flutter_sdk: ^0.4.2
AndroidManifest.xml
  추가해야할 것 첫 번째
          <activity android:name="com.kakao.sdk.flutter.AuthCodeCustomTabsActivity">
            <intent-filter android:label="flutter_web_auth"> <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="kakao{native App Key}" android:host="oauth"/>
            </intent-filter>
        </activity>
  추가해야할 것 두 번째
          <meta-data
            android:name="com.kakao.sdk.AppKey"
            android:value="{native App key}" />
  추가해야할 것 세 번째
      <uses-permission android:name="android.permission.INTERNET" />
  체크해야할 것
    package name이 kakao developer application의 패키지명과 일치하는지 확인.


debug/release용 hash key 일치X : 로그인 안됨.
  debug hash key 얻는 방법.
  파일 열기 -> 해당 프로젝트 -> android 선택하여 열기
  java 폴더에 MainActivity.java class 생성.
  아래 함수 추가하고 onCreate 함수에서 getAppKeyHash() 함수 호출하고, console log로 hash key 확인.
  developer -> 플랫폼 -> android -> 키해시 log에 나온 hash key 넣기.

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
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {

            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }
 */

void main() {
  KakaoContext.clientId = 'Your App Client Id';
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              onPressed: () {
                Navigator.push(context, MaterialPageRoute(
                    builder: (context) => new KakaoLogin()
                ));
              },
              child: Text("로그인"),
            )
          ],
        ),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
