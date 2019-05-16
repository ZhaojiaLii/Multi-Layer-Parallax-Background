package com.example.multi_layerparallax

import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.*
import kotlin.math.abs

class ScrollingActivity : AppCompatActivity() {

    private lateinit var content : LinearLayout
    private lateinit var top_btn : Button
    private lateinit var top_btn_after_trans : ImageButton
    private lateinit var rootView : CustomeCoordinatorLayout
    private lateinit var header : View
    private lateinit var top_title : TextView
    private lateinit var top_btn_text : TextView
    private lateinit var toolbar : Toolbar
    private lateinit var real_content : NestedScrollView

    private var realContentInitPosition = 0f
    private var toolbar_statusbar_height = 0f

    var old_padding_icon = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        init()


        //----------------------------------  postDelayed(Runnable,long)  --------------------------------------
        val handler1 = Handler()
        val runnable1 = Runnable {
            realContentInitPosition = getViewPositionY(real_content).toFloat()
            toolbar_statusbar_height = toolbar.layoutParams.height + getStatusBarHeight()
            old_padding_icon = top_btn.paddingLeft  //35
            println("real content $realContentInitPosition")
        }
        handler1.postDelayed(runnable1,100)

        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {

                val changedY = getViewPositionY(real_content) - realContentInitPosition
                println("real content $changedY")
                //=================
                val threshold = 450
                if (getViewPositionY(real_content)<=threshold){
                    val temp = threshold-toolbar_statusbar_height
                    val ratio = 1-(getViewPositionY(real_content)-toolbar_statusbar_height)/temp
                    top_title.alpha = ratio
                }else{
                    top_title.alpha = 0f
                }
                if (getViewPositionY(real_content).toFloat() == toolbar_statusbar_height){
                    top_title.alpha = 1f
                }
                content.scrollY = (changedY/5).toInt()
                //======================


                val threshold1 = 150
                val threshold2 = 350
                val threshold3 = 270
                if (abs(changedY)<=threshold1){
                    top_btn_text.alpha = 1-abs(changedY)/threshold1
                    top_btn.alpha = 1f
                    top_btn_after_trans.alpha = 0f
                    top_btn.setPadding(35,0,0,0)
                }else{
                    top_btn_text.alpha = 0f
                    if (abs(changedY)<=threshold2){
                        top_btn.alpha = 1-(abs(changedY)-threshold1)/(threshold2-threshold1)
                        val ratio = ((threshold3.toFloat()-old_padding_icon.toFloat())/(threshold2.toFloat()-threshold1.toFloat()))  //70/200
                        val padding = old_padding_icon + (abs(changedY)-threshold1)*ratio
                        println("padding is $padding , ratio is $ratio , ${threshold2-threshold1}")
                        top_btn.setPadding(padding.toInt(),0,0,0)
                        when {
                            abs(changedY) in 280.0..320.0 -> top_btn_after_trans.alpha = (abs(changedY)-280)/40
                            abs(changedY)>320 -> top_btn_after_trans.alpha = 1f
                            abs(changedY)<280 -> top_btn_after_trans.alpha = 0f
                        }
                    }else{
                        top_btn.alpha = 0f
                        top_btn_after_trans.alpha = 1f
                    }
                }


                handler.postDelayed(this, 1)
            }
        }
        handler.postDelayed(runnable,1)


//----------------------------------  postDelayed(Runnable,long)  --------------------------------------


        top_btn.setOnClickListener { Toast.makeText(applicationContext,"favoris clicked",Toast.LENGTH_SHORT).show() }
        top_btn_text.setOnClickListener { Toast.makeText(applicationContext,"favoris clicked",Toast.LENGTH_SHORT).show() }
        top_btn_after_trans.setOnClickListener { Toast.makeText(applicationContext,"favoris clicked",Toast.LENGTH_SHORT).show() }



    }




    fun init(){
        content = findViewById(R.id.header_content)
        rootView = findViewById(R.id.RootView)
        header = findViewById(R.id.header_background)
        real_content = findViewById(R.id.real_content)
        toolbar = findViewById(R.id.toolbar)
        top_btn = findViewById(R.id.top_btn)
        top_btn_after_trans = findViewById(R.id.top_btn_after_trans)
        top_title = findViewById(R.id.top_title)
        top_btn_text = findViewById(R.id.top_btn_text)
        addStatusBarHeight(top_title)
        //addStatusBarHeight(top_btn)

        rootView.getContent(content,header,real_content)

        setSupportActionBar(toolbar)

    }

    fun addStatusBarHeight(view:View){
        val height = view.layoutParams.height
        view.layoutParams.height = (height+getStatusBarHeight()).toInt()
    }

    fun getStatusBarHeight():Float{
        var height = 0
        val resourceID : Int = applicationContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceID > 0){
            height = applicationContext.resources.getDimensionPixelSize(resourceID)
        }
        return height.toFloat()
    }

    fun getViewPositionY(view: View):Int{
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        return position[1]
    }
}
