package com.example.multi_layerparallax

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

open class CustomeCoordinatorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    private lateinit var content : LinearLayout
    private lateinit var header : View
    private lateinit var realcontent : View

    private var isTouched = false
    private var isDragging = false
    private var initX = 0f
    private var initY = 0f

    private var headerInitPosition = 0f
    private var headerContentInitPosition = 0f
    var realContentInitPosition = 0f


    fun getContent (content : LinearLayout, header:View, realcontent : View){
        this.content = content
        this.header = header
        this.realcontent = realcontent
        content.post {
            run{
                headerInitPosition = getViewPositionY(header).toFloat()
                headerContentInitPosition = getViewPositionY(content).toFloat()
                realContentInitPosition = getViewPositionY(realcontent).toFloat()
                System.out.println("init positions get : header-->$headerInitPosition, header content-->$headerContentInitPosition, real content-->$realContentInitPosition")
            }
        }
    }




    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        when(ev!!.action){
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                isDragging = false
                initX = ev.x
                initY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                isDragging = true
                val draggedX = ev.x - initX
                val draggedY = ev.y - initY

                if (draggedY < 0){
                    return true
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> {}
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when(ev!!.action){
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                val draggedX = ev.x - initX
                val draggedY = ev.y - initY
                printMessage("$draggedY")
                printMessage("content x: ${getViewPositionX(content)}, y: ${getViewPositionY(content)}")
                printMessage("header image x: ${getViewPositionX(header)}, y: ${getViewPositionY(header)}")
                printMessage("real content x: ${getViewPositionX(realcontent)}, y: ${getViewPositionY(realcontent)}")
                //moveContent()
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> {}
        }
        return super.onTouchEvent(ev)
    }

    fun moveContent(){

        val temp = getViewPositionY(realcontent) - getViewPositionY(content)
        val changedY = getViewPositionY(realcontent) - realContentInitPosition
        val ratio = 0.5f
    }

    fun getViewPositionY(view: View):Int{
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        return position[1]
    }
    fun getViewPositionX(view: View):Int{
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        return position[0]
    }

    fun printMessage(content:String){
        System.out.println(content)
    }
}