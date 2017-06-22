package com.zoftino.rxjavaretrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreService {
    public static final String BASE_URL = "http://www.zoftino.com/api/";
    private static Retrofit retrofit = null;


    public static Retrofit getCouponClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
