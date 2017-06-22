package com.zoftino.rxjavaretrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String BASE_URL = "http://www.zoftino.com/api/";

    //keeps track of subscriptions
    private CompositeDisposable compositeDisposable;

    private RecyclerView couponRecyclerView;

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set layout manager for recyclerView
        couponRecyclerView = (RecyclerView) findViewById(R.id.coupon_rv);
        RecyclerView.LayoutManager couponLayoutManager = new LinearLayoutManager(this);
        couponRecyclerView.setLayoutManager(couponLayoutManager);

        //configure Retrofit using Retrofit Builder
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void showCoupons(View view){
        getcouponData();
    }
    public void showCouponsTopStore(View view){
        getStoreCouponData();
    }
    //two Retrofit service calls execute parallel using RxJava
    private void getStoreCouponData(){
        //first it creates an observable which emits retrofit service class
        //to leave current main thread, we need to use subscribeOn which subscribes the observable on computation thread
        //flatMap is used to apply function on the item emitted by previous observable
        //function makes two rest service calls using the give retrofit object for defined api interface
        //these two calls run parallel that is why subscribeOn is used on each of them
        //since these two api call return same object, they are joined using concatArray operator
        //finally consumer observes on android main thread
        Observable.just(retrofit.create(StoreCouponsApi.class)).subscribeOn(Schedulers.computation())
                .flatMap(s -> {
            Observable<StoreCoupons> couponsObservable
                    = s.getCoupons("topcoupons").subscribeOn(Schedulers.io());

            Observable<StoreCoupons> storeInfoObservable
                    = s.getStoreInfo().subscribeOn(Schedulers.io());

             return Observable.concatArray(couponsObservable,storeInfoObservable);
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResults, this::handleError );

    }
    //single api call using retrofit and rxjava
    private void getcouponData(){
        retrofit.create(StoreCouponsApi.class).getCoupons("topcoupons")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResults, this::handleError );
    }

    private void handleResults(StoreCoupons storeCoupons){
        if(storeCoupons.getCoupons() != null){
            CouponsAdapter ca = new CouponsAdapter(storeCoupons.getCoupons(), MainActivity.this);
            couponRecyclerView.setAdapter(ca);
        }else{
            TextView store_name = (TextView) findViewById(R.id.store_name);
            store_name.setText(storeCoupons.getStore());
            TextView coupon_count = (TextView) findViewById(R.id.coupon_count);
            coupon_count.setText(storeCoupons.getTotalCoupons());
            TextView max_cashback = (TextView) findViewById(R.id.max_cashback);
            max_cashback.setText(storeCoupons.getMaxCashback());
        }
    }

    private void handleError(Throwable t){
       Log.e("Observer", ""+ t.toString());
        Toast.makeText(this, "ERROR IN GETTING COUPONS",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        //dispose subscriptions
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        super.onDestroy();
    }
}
