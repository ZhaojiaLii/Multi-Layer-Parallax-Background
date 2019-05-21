package com.example.multi_layerparallax

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.multi_layerparallax.Interfaces.SearchRequestInterface.Companion.service
import com.example.multi_layerparallax.Interfaces.WikiApiService
import com.example.multi_layerparallax.Models.IpModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.search_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CallHTTPrequest : AppCompatActivity() {

    private var disposable: Disposable? = null   // disposable is a cancelable object

    private val wikiApiServe by lazy {
        WikiApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_page)

        get_result.setOnClickListener {
            val text = edit_text.text.toString()
            if (edit_text.text.toString().isNotEmpty()){
                Toast.makeText(this, edit_text.text.toString(), Toast.LENGTH_LONG).show()
                beginSearch(text)
            }
        }

        get_result_2.setOnClickListener {
            val text_get_2 = edit_text_2.text.toString()
            if (text_get_2.isNotEmpty()){
                beginSearch2(text_get_2)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun beginSearch(srsearch : String){
        val options = HashMap<String,String>()
        options["action"] = "query"
        options["format"] = "json"
        options["list"] = "search"
        options["srsearch"] = srsearch

        //Scheduler: Thread control
        disposable = wikiApiServe.hitCountCheck(options)
            .subscribeOn(Schedulers.io())  //subscribeOn(): choose a Thread to produce  I/O scheduler:（read&write data、read&write db、change info on the Internet）
            .observeOn(AndroidSchedulers.mainThread())  //observeOn(): choose a Thread to spend  AndroidSchedulers.mainThread()，on Android UI(main Thread)
            .subscribe(
                { result -> show_result.text = "${result.query.searchinfo.totalhits} results found"},
                { error -> Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()}
            )
    }

    private fun beginSearch2(srsearch : String){

        val options = HashMap<String,String>()
        options["action"] = "query"
        options["format"] = "json"
        options["list"] = "search"
        options["srsearch"] = srsearch

        val call = service.search(options)

//        call.enqueue(object :Callback{
//            override fun onResponse(call: Call<IpModel>, response: Response<IpModel>) {
//                Toast.makeText(applicationContext, response.body().toString(), Toast.LENGTH_LONG).show()
//            }
//
//            override fun onFailure(call: Call<IpModel>, t: Throwable) {
//                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
//            }
//
//        })

    }

    override fun onDestroy() {
        // cancel subscription in order to prevent from leaking of RAM
        if (disposable!=null&&!disposable!!.isDisposed){
            disposable?.dispose()
        }
        super.onDestroy()

    }






}