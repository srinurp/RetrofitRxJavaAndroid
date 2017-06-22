package com.zoftino.rxjavaretrofit;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StoreCouponsApi {
    @GET("coupons/")
    Observable<StoreCoupons> getCoupons(@Query("status") String status);
    @GET("storeOffers/")
    Observable<StoreCoupons> getStoreInfo();
}
