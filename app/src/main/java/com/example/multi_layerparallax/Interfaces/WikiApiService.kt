package com.example.multi_layerparallax.Interfaces

import com.example.multi_layerparallax.BuildConfig
import com.example.multi_layerparallax.Models.Model
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.concurrent.TimeUnit

interface WikiApiService{

    @GET("api.php")
    fun hitCountCheck(@QueryMap options:Map<String,String>):Observable<Model.Result>
    //an Observable, which is a Rxjava object that could analog as the endpoint fetcher result generator.
    companion object{
        fun create() :WikiApiService{
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://en.wikipedia.org/w/")
                .build()
            return retrofit.create(WikiApiService::class.java)
        }
    }

}