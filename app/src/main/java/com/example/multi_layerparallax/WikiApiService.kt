package com.example.multi_layerparallax

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiApiService{
    @GET("api.php")
    fun hitCountCheck(@Query("action") action:String,
                      @Query("format") format:String,
                      @Query("list") list: String,
                      @Query("srsearch") srsearch: String): Observable<Model.Result>

    //a Observable, which is a Rxjava object that could analog as the endpoint fetcher result generator.

    companion object{
        fun create():WikiApiService{
            val retrofit = Retrofit
                .Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://en.wikipedia.org/w/")
                .build()
            return retrofit.create(WikiApiService::class.java)
        }

    }

}