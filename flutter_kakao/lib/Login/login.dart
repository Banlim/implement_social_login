
import 'package:flutter/material.dart';
import 'package:flutter_kakao/Model/kakao_user_model.dart';
import 'package:kakao_flutter_sdk/all.dart';

class Login extends StatefulWidget {
  @override
  _LoginState createState() => _LoginState();
}

class _LoginState extends State<Login> {
  KakaoUserModel userModel;

  Future<bool> _getUser() async {
    try{
      await UserApi.instance.me()
          .then((user) {
        setState(() {
          String nickname = user.kakaoAccount.profile.toJson()['nickname'].toString();
          String email = user.kakaoAccount.email.toString();
          String gender = user.kakaoAccount.gender.toString();
          String birthday = user.kakaoAccount.birthday.toString();
//          String birthyear = user.kakaoAccount.birthyear.toString();
//          String phoneNumber = user.kakaoAccount.phoneNumber.toString();
          userModel = KakaoUserModel(nickname, email, gender, birthday, '1998', '01012345678');
        });
      });
    } on KakaoAuthException catch (e){
    }catch (e){

    }
  }
  @override
  Widget build(BuildContext context) {
    _getUser();
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Container(
            alignment: Alignment.center,
              child: Text('nickname is : ${userModel.nickName}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('email is : ${userModel.email}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('gender is : ${userModel.gender}'),
          ),
          Container(
            alignment: Alignment.center,
            child: Text('birthday is : ${userModel.birthday}'),
          ),
//          Container(
//            alignment: Alignment.center,
//            child: Text('birthyear is : ${userModel.birthyear}'),
//          ),
//          Container(
//            alignment: Alignment.center,
//            child: Text('phoneNumber is : ${userModel.phoneNumber}'),
//          )
        ],
      ),
    );
  }
}
