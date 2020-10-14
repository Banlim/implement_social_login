package com.example.naver_with_retrofit2;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;

public interface NaverDataApi {

    @GET("me")
    Single<NaverUserModel> get_Singleitem();

    @GET("me")
    Observable<NaverUserModel> get_Observableitem();

}
