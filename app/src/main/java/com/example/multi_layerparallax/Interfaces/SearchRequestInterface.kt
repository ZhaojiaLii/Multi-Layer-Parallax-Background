package com.example.multi_layerparallax.Interfaces

import com.example.multi_layerparallax.Models.IpModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface SearchRequestInterface {

    @GET("api.php")
    fun search(@QueryMap options:Map<String,String>):okhttp3.Call

    companion object{
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://en.wikipedia.org/w/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SearchRequestInterface::class.java)
    }



}