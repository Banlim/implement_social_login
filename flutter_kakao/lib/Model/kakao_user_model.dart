

class KakaoUserModel{
  String _nickName;
  String _email;
  String _gender;
  String _birthday;
  String _birthyear;
  String _phoneNumber;


  KakaoUserModel(String _nickName, String _email, String _gender, String _birthday,
      String _birthyear, String _phoneNumber){
    this._nickName = _nickName;
    this._email = _email;
    this._gender = _gender;
    this._birthday = _birthday;
    this._birthyear = _birthyear;
    this._phoneNumber = _phoneNumber;
  }

  String get nickName => _nickName;

  String get email => _email;

  String get phoneNumber => _phoneNumber;

  String get birthyear => _birthyear;

  String get birthday => _birthday;

  String get gender => _gender;
}