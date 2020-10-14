# Facebook social login (with firebase)


### 추가할 사항

* Firebase에서 본인의 console을 생성한 후, google-services.json 파일을 해당 프로젝트의 app directory 아래에 복사한다.

* build.gradle(:project)
  ~~~gradle
  buildscript {
    repositories {
        . . .
        mavenCentral()
    }
    dependencies {
        . . .
        classpath 'com.google.gms:google-services:4.3.4'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
  }
  ~~~
  
* build.gradle(:app)
  ~~~gradle
  apply plugin: 'com.google.gms.google-services'
  dependencies {
    . . .
    implementation 'com.google.firebase:firebase-analytics:17.5.0'
    implementation 'com.google.firebase:firebase-auth:19.4.0'
    implementation 'com.facebook.android:facebook-android-sdk:7.1.0'
    implementation 'com.google.android.gms:play-services-auth:16.0.1'
  }
  ~~~
  
* AndroidManifest.xml
  ~~~xml
  <uses-permission android:name="android.permission.INTERNET" />
  
  
  
