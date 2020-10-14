package com.example.naver_with_retrofit2;

import java.io.Serializable;

/*
GET으로 반환된 response 값? json 형태로 오는 걸 그대로 Model화 해야함...
네이버 로그인의 경우 응답이 아래와 같은 형태로 오기 때문에 Model class도 이렇게 구성함.
{
  "resultcode": "00",
  "message": "success",
  "response": {
    "email": "openapi@naver.com",
    "nickname": "OpenAPI",
    "profile_image": "https://ssl.pstatic.net/static/pwe/address/nodata_33x33.gif",
    "age": "40-49",
    "gender": "F",
    "id": "32742776",
    "name": "오픈 API",
    "birthday": "10-01"
  }
}
 */
class NaverUserModel {

    private String resultcode;
    private String message;
    private UserModel response;

    public String getResultcode() {
        return resultcode;
    }

    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserModel getResponse() {
        return response;
    }

    public void setResponse(UserModel response) {
        this.response = response;
    }

    public class UserModel implements Serializable {
        private String nickname;
        private String email;
        private String gender;
        private String birthday;

//        public UserModel(String nickname, String email, String gender, String birthday) {
//            this.nickname = nickname;
//            this.email = email;
//            this.gender = gender;
//            this.birthday = birthday;
//        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }
    }

}
