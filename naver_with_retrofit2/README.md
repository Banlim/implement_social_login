# Naver social login with Rxjava2 + Retrofit2


### 추가할 사항
* 네이버 로그인 관련하여 추가할 사항 아래 링크 참조
  * [네이버 로그인](https://github.com/Banlim/implement_social_login/blob/main/naver_login/README.md)
  
* build.gradle(:app)
  ~~~gradle
  android {
  
    . . .
    // 아래 코드 추가해야 "NoSUchMethodError: No Static method metafactory" 에러 
    compileOptions{
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
  }
  dependencies {
    implementation 'com.squareup.retrofit2:converter-gson:2.8.1'
    implementation 'com.squareup.retrofit2:retrofit:2.8.1'
    implementation 'com.google.code.gson:gson:2.8.6'

    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.19'
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.8.1'

    implementation 'com.squareup.okhttp3:logging-interceptor:3.12.1'
  }
  ~~~
  
* build.gradle(:project)
  ~~~gradle
  allprojects {
    repositories {
        maven { url "https://oss.jfrog.org/libs-snapshot"}
    }
  }
  ~~~
  
  
### 주 내용

* User Profile 조회하는 부분을 Rxjava2 + Retrofit2로 수정
  * NaverDataApi.java
    * GET 통신을 하기 위한 Interface 작성
    * Single, Observable 둘 중에 원하는 것으로 사용하면 됨.
    ~~~java
    public interface NaverDataApi {
      @GET("me")
      Single<NaverUserModel> get_Singleitem();

      @GET("me")
      Observable<NaverUserModel> get_Observableitem();
    }
    ~~~
  
  * RestfulAdapter.java
    * Retrofit2를 사용하여 api를 얻는 class
    * Retrofit2에 내장되어 있는 Okhttp3를 통해 http 통신을 하기 위해 정리한? class
    ~~~java
    public class RestfulAdapter {
    private String TAG = RestfulAdapter.class.getName();
    private String baseUrl = "https://openapi.naver.com/v1/nid/";

    private static class Singleton{
        private static final RestfulAdapter instance = new RestfulAdapter();
    }

    public static RestfulAdapter getInstance(){
        return Singleton.instance;
    }

    public NaverDataApi getApi(String token){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor)
                .addInterceptor(getHeaderInterceptor(token));

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .baseUrl(baseUrl)
                .build();

        return retrofit.create(NaverDataApi.class);
    }

    private Interceptor getHeaderInterceptor(final String token) {
        Interceptor interceptor;
        interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest;
                String apiToken = "Bearer " + token;
                if (token != null && !token.equals("")) {
                    newRequest = chain.request().newBuilder().addHeader("Authorization", apiToken).build();
                } else {
                    newRequest = chain.request();
                }
                return chain.proceed(newRequest);
            }
        };
        return interceptor;
      }
    }
    ~~~
    
  * startSingleRxGetData(String token)
    * Rxjava2의 Observable중 Single 형태로 비동기 통신을 하여 User Profile을 조회하는 method
    ~~~java
    private void startSingleRxGetData(String token){
        NaverDataApi service = RestfulAdapter.getInstance().getApi(token);
        Single<NaverUserModel> single = service.get_Singleitem();

        disposable = single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> Log.e("DoOnError : ", e.getMessage()))
                .subscribe(items -> {
                    if(items.getResponse() != null){
                        Log.d(TAG, "nickname : " + items.getResponse().getNickname());
                        model = items.getResponse();
                        loginDoneActivity();
                    }
                });
    }
    ~~~
    
  * startObservableRxGetData(String token)
    * Rxjava2의 Observable 형태로 비동기 통신을 하여 User Profile을 조회하는 method
    ~~~java
    private void startObservableRxGetData(String token){
        NaverDataApi service = RestfulAdapter.getInstance().getApi(token);
        Observable<NaverUserModel> observable = service.get_Observableitem();

        mCompositeDisposable.add(observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<NaverUserModel>(){

                    @Override
                    public void onNext(NaverUserModel naverUserModel) {
                        model = naverUserModel.getResponse();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "complete");
                        // adapter 이런 거 있으면 setItems(model); 해서 사용
                        loginDoneActivity();
                    }
                })
        );
    }
    ~~~
  
  * OAuthLoginHandler
    * 여기서 run method가 success할 때 accessToken을 얻을 수 있다.
    * 이 때, Single/Observable 둘 중 골라서 아래와 같이 호출하면 사용할 수 있다.
    ~~~java
    new OAuthLoginHandler(){
      public void run(boolean success) {
        if (success) {
          String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
        
          // Single
          startSingleRxGetData(accessToken);
        
          // Observable
          startObservableRxGetData(accessToken);
        }
        . . .
      }
    }
    ~~~
    
  * ~~Activity.java
    * 사용하고 나면 dispose()를 꼭 해준다.
    ~~~java
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        mCompositeDisposable.dispose();
    }
    ~~~

* 번외로, Web 통신을 할 때 RequestBody를 넣어서 요청해야할 경우
  * TestApi.java (각자 api에)
    * Api Interface에 annotation @BODY 추가
    ~~~java
    @POST("your url")
    Single<DataModel> get_item(@Body RequestBody body);
    ~~~
    
  * RequestBody.java
    * body_field는 필요한 만큼 추가
    ~~~java
    public class RequestBody {
      private int body_field1;
      private String body_field2;
      private int body_field3;

      public RequestBody(int body_field1, String body_field2) {
        this.body_field1 = body_field1;
        this.body_field2 = body_field2;
      }

      public RequestBody(int body_field1, String body_field2, int body_field3) {
        this.body_field1 = body_field1;
        this.body_field2 = body_field2;
        this.body_field3 = body_field3;
      }
    }
    ~~~
  * rxjava2로 비동기 통신
    * 아래 코드와 같이 RequestBody class를 생성하여 api를 호출한다.
    ~~~java
    Single<DataModel> single = service.get_item(new RequestBody(field1, field2, field3));
    ~~~
    
### 참조한 링크
  * [네트워크 요청 결과 RxJava로 처리하기](https://eclipse-owl.tistory.com/24)
  * [Rxjava2에 Retrofit2 적용하기](https://poqw.github.io/RxJava2_3/)
  * [안드로이드의 RxJava 활용](https://beomseok95.tistory.com/59)
