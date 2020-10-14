package com.example.naver_with_retrofit2;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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
