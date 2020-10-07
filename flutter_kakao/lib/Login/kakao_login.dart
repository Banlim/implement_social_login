import 'package:flutter/material.dart';
import 'package:kakao_flutter_sdk/all.dart';

import 'login.dart';

class KakaoLogin extends StatefulWidget {
  @override
  _KakaoLoginState createState() => _KakaoLoginState();
}

class _KakaoLoginState extends State<KakaoLogin> {
  bool _isKakaoTalkInstalled = false;

  @override
  Widget build(BuildContext context) {
    isKakaoTalkInstalled();
    return Scaffold(
      appBar: AppBar(
        title: Text("Kakao social login"),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RaisedButton(
              child: Text("카카오톡으로 시작하기"),
              onPressed:
              _isKakaoTalkInstalled ? _loginWithTalk : _loginWithKakao),
          RaisedButton(
              child: Text("로그아웃"),
              onPressed:
              logOutTalk),
        ],
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    _initKakaoTalkInstalled();
  }

  _initKakaoTalkInstalled() async {
    final installed = await isKakaoTalkInstalled();
    setState(() {
      _isKakaoTalkInstalled = installed;
    });
  }

  _loginWithKakao() async {
    try {
      String authCode;
      await AuthCodeClient.instance.request().then((value) {
        authCode = value;
      });
      await _issueAccessToken(authCode);
    } catch (e) {
      print('loginWithKakao : ' + e);
    }
  }

  _loginWithTalk() async {
    try {
      String authCode =
          await AuthCodeClient.instance.requestWithTalk().catchError((e) {
      });
      await _issueAccessToken(authCode);
    } catch (e) {
      print('loginWithTalk : ' + e);
    }
  }

  _issueAccessToken(String authCode) async {
    try {
      await AuthApi.instance.issueAccessToken(authCode).then((token) {
        setState(() {
          AccessTokenStore.instance.toStore(token);
        });
      });
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => Login()),
      );
    } catch (e) {
      print('error on issuing access token : $e');
    }
  }

  logOutTalk() async {
    try {
      var code = await UserApi.instance.logout();
      print(code.toString());
    } catch (e) {
      print('logout : $e');
    }
  }
}
