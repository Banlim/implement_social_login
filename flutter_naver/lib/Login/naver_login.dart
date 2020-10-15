import 'package:flutter/material.dart';
import 'package:flutter_naver_login/flutter_naver_login.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;

class NaverLogin extends StatefulWidget {
  @override
  _NaverLoginState createState() => _NaverLoginState();
}

class _NaverLoginState extends State<NaverLogin> {

  bool isLogin = false;
  String accessToken;
  String tokenType;
  String name;

  String nickName = "nickName";
  String email = "email";
  String gender = "gender";
  String birthday = "birthday";


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Naver social login"),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Container(
            alignment: Alignment.center,
            child: RaisedButton(
              child: Text("Naver로 시작하기"),
              onPressed: _loginWithNaver,
            ),
          ),
          Container(
            alignment: Alignment.center,
            child: RaisedButton(
              child: Text("로그아웃"),
              onPressed: _logoutWithNaver,
            ),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('nickname is : ${nickName}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('email is : ${email}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('gender is : ${gender}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('birthday is : ${birthday}'),
          )
        ],
      ),
    );
  }
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
      print('logout?');
    });
  }



  Future<void> _getUser() async {
    NaverAccountResult res = await FlutterNaverLogin.currentAccount();
    setState(() {
      nickName = res.nickname;
      email = res.email;
      gender = res.gender;
      birthday = res.birthday;
    });
  }
}
