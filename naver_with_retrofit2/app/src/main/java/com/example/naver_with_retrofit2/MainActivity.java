package com.example.naver_with_retrofit2;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.GsonBuilder;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

import static com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler;

public class MainActivity extends AppCompatActivity {

    /*
    일단 logout은 구현 X
    GetUser 부분에 RxJava2 + Retrofit2 적용.
    RxJava3 쓰려고 했으나 아직 Retrofit2랑 호환?이 잘 안된다는 걸 본 것 같아서 2로 사용.

    ## 추가
        build.gradle(:app)
        아래 코드 추가해야 "NoSuchMethodError: No static method metafactory" 해결 가능.
        compileOptions{
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }

    * Rxjava & Retrofit implementation
        dependencies{
            implementation 'com.squareup.retrofit2:converter-gson:2.8.1'
            implementation 'com.squareup.retrofit2:retrofit:2.8.1'
            implementation 'com.google.code.gson:gson:2.8.6'

            implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
            implementation 'io.reactivex.rxjava2:rxjava:2.2.19'
            implementation 'com.squareup.retrofit2:adapter-rxjava2:2.8.1'
        }

    build.gradle(:project)
        maven { url "https://oss.jfrog.org/libs-snapshot"}

    Manifest.xml
        <uses-permission android:name="android.permission.INTERNET"/>
     */

    private String TAG = "MAIN_ACTIVITY";
    private String OAUTH_CLIENT_ID;
    private String OAUTH_CLIENT_SECRET;
    private String OAUTH_CLIENT_NAME;

    private static OAuthLogin mOAuthLoginInstance;
    private OAuthLoginButton mOAuthLoginButton;
    private Context mContext;
    private NaverUserModel.UserModel model;

    private Disposable disposable;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        init();
    }

    private void init() {
        OAUTH_CLIENT_ID = mContext.getString(R.string.client_id);
        OAUTH_CLIENT_SECRET = mContext.getString(R.string.client_secret);
        OAUTH_CLIENT_NAME = mContext.getString(R.string.client_name);

        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.naver_login_btn);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
        mCompositeDisposable = new CompositeDisposable();
    }

    // 기존 코드와 동일함.
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                Log.d(TAG, "success : " + accessToken);
                Log.d(TAG, "expiresAt : " + Long.toString(expiresAt));

                // 둘 중에 원하는 것 사용
                startSingleRxGetData(accessToken);
//                startObservableRxGetData(accessToken);

//                getUser(accessToken);
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Log.d(TAG, "errorCode : " + errorCode);
                Log.d(TAG, "errorDesc : " + errorDesc);
            }
        }
    };

    protected void loginDoneActivity() {
        Intent intent = new Intent(MainActivity.this, LoginDoneActivity.class);
        intent.putExtra("Data", model);
        startActivity(intent);
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        mCompositeDisposable.dispose();
    }

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


    /*
    HttpUrlConnection 쓰는 부분인 GetUser 부분만 일단 rxjava2 + retrofit2 적용.
    좀 코드 잘못쓴 것 같음. 아래 코드는 그냥 Retrofit2만 썼다고 해도 무방.
    Rxjava를 제대로 이용하지 못한 것 같음.
     */

    /*
    private void getUser(final String token) {
        Observable.defer(new Callable<ObservableSource<?>>() {
            @Override
            public ObservableSource<?> call() throws Exception {
                // 아마 이 부분이? Asynctask의 doInBackground() 부분과 비슷한 듯 하다.
                Log.d(TAG, "Observer Call");
                getUserTask(token);
                return Completable.complete().toObservable();
            }
        })
                .doOnError(e -> Log.e("DoOnError : ", e.getMessage()))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }


    private void getUserTask(String token) {
        Log.d(TAG, "access getUserTask");
        GsonBuilder gsonBuilder = new GsonBuilder();
        // url의 마지막에 '/' 넣지 않으면 에러 발생한다고 본 듯 함.
        String url = "https://openapi.naver.com/v1/nid/";

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()));
        Interceptor interceptor = getHeaderInterceptor(token);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();
        retrofitBuilder.client(client);

        Retrofit retrofit = retrofitBuilder.build();
        NaverDataApi api = retrofit.create(NaverDataApi.class);
        Call<NaverUserModel> call = api.get_item();
        call.enqueue(new Callback<NaverUserModel>() {
            @Override
            public void onResponse(Call<NaverUserModel> call, retrofit2.Response<NaverUserModel> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successful");
                    model = response.body().getResponse();
//                    String nickname = response.body().getResponse().getNickname();
//                    String email = response.body().getResponse().getEmail();
//                    String gender = response.body().getResponse().getGender();
//                    String birthday = response.body().getResponse().getBirthday();
//                    model = NaverUserModel.UserModel
                    loginDoneActivity();
                }
            }

            @Override
            public void onFailure(Call<NaverUserModel> call, Throwable t) {
                Log.d(TAG, "FAILED");
            }
        });
    }

    // Authorization의 경우 Http 요청 과정에서 지속적으로 token을 Header에 추가하여 요청하기 위해 Interceptor 사용
    // @Header annotation을 설정하여 요청하는 방법도 존재함.
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

     */
}