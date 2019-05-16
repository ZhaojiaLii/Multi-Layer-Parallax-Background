package com.example.multi_layerparallax

import android.annotation.SuppressLint
import android.app.SearchableInfo
import android.graphics.ColorSpace
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CallHTTPrequest : AppCompatActivity() {

    private lateinit var edit_text : EditText
    private lateinit var get_result : Button
    private lateinit var show_result : TextView

    val wikiApiServe by lazy {
        WikiApiService.create()
    }
    var disposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_page)

        edit_text = findViewById(R.id.edit_text)
        get_result = findViewById(R.id.get_result)
        show_result = findViewById(R.id.show_result)

        get_result.setOnClickListener {
            val text_get = edit_text.text.toString()
            if (text_get.isNotEmpty()){
                beginSearch(text_get)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun beginSearch(srsearch : String){
        disposable = wikiApiServe
            .hitCountCheck("query", "json", "search", srsearch)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> show_result.text = "${result.query.searchInfo.totalhits} results found"},
                { error -> Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()}
            )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }





}