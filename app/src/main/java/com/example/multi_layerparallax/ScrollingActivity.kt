package com.example.multi_layerparallax

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_scrolling.*

class ScrollingActivity : AppCompatActivity() {

    private lateinit var content : LinearLayout
    private lateinit var rootView : CustomeCoordinatorLayout
    private lateinit var header : View
    private lateinit var top_title : TextView
    private lateinit var toolbar : Toolbar
    private lateinit var real_content : NestedScrollView

    private var realContentInitPosition = 0f
    private var toolbar_statusbar_height = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        init()



        //----------------------------------  postDelayed(Runnable,long)  --------------------------------------
        val handler1 = Handler()
        val runnable1 = Runnable {
            realContentInitPosition = getViewPositionY(real_content).toFloat()
            toolbar_statusbar_height = toolbar.layoutParams.height + getStatusBarHeight()
        }
        handler1.postDelayed(runnable1,100)

        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {

                val changedY = getViewPositionY(real_content) - realContentInitPosition
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
                handler.postDelayed(this, 1)
            }
        }
        handler.postDelayed(runnable,1)


//----------------------------------  postDelayed(Runnable,long)  --------------------------------------




    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun init(){
        content = findViewById(R.id.header_content)
        rootView = findViewById(R.id.RootView)
        header = findViewById(R.id.header_background)
        real_content = findViewById(R.id.real_content)
        toolbar = findViewById(R.id.toolbar)
        top_title = findViewById(R.id.top_title)
        addStatusBarHeight(top_title)

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
