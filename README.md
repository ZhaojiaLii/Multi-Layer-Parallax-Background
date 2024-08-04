# Multi-Layer-Parallax-Background
## first off all
Because the company is making mobile applications for movie theaters, there is a need to make the multi-layer parallax background shown in the figure, which is native and does not use third-party libraries. So the first thing that comes to mind is to directly use Google's official CoordinatorLayout + AppbarLayout + CollapsingLayout to achieve the most basic parallax background effect.


Platform：Android Studio, Language：Kotlin

## Ideas
1. First, use Google's official CoordinatorLayout + AppbarLayout + CollapsingLayout layout to achieve a basic collapsible layout. You can customize the size and layout of the background image. To achieve parallax, simply add the "parallax" attribute. 

2. Then it is about how to add this additional layer of background layout and give it a different moving speed, so that it seems to us that there are three layers (background + background layer content + specific content below) layouts with three different moving speeds, creating a multi-layer parallax effect.
   
3. Adding content to the background layer is easy. Just create a new LinearLayout, construct the layout of the content, and include it in the AppbarLayout. Because the essence of the source code of this part actually extends a FrameLayout, we can overlap multiple layers of content at the header position. Then obtain the ID of the content in the MainActivity, and this step is completed.

4. The following is the most important step. How to make the layout of this part have different upward sliding speeds and be able to monitor the bottom position at any time during the inertial sliding process and change its own position.

    4.1. Rewrite CoordinatorLayout, rewrite `InterceptedTouchEvent()` and `onTouchEvent()`, and don't worry about `dispatchTouchEvent()` for now. We intercept the finger actions on the screen in `InterceptedTouchEvent()`, and then distribute the events according to our requirements. If it is detected that the finger is swiping upwards, `return true` to pass the event to `onTouchEvent()` for processing.
    
    4.2 In the new CoordinatorLayout, an open function needs to be written to enable the Activity to pass the header background image over. Only in this way can we normally handle the image position and obtain relevant information in the newly created layout. This is very important; otherwise, we cannot find the code position of the background image in this file (cannot findViewById). 
    ```kotlin
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
    ```
    
    4.3 In `onTouchEvent()`, detect the position change of the bottom in real time. This requires us to pass all the information of the three layers of content in the method defined in 4.2 to facilitate our detection and modification in the layout. 
    
    InterceptTouchEvent()
    ```kotlin
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
    ```
    onTouchEvent()
    ```kotlin
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when(ev!!.action){
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                val draggedX = ev.x - initX
                val draggedY = ev.y - initY
                println("$draggedY")
                println("content x: ${getViewPositionX(content)}, y: ${getViewPositionY(content)}")
                println("header image x: ${getViewPositionX(header)}, y: ${getViewPositionY(header)}")
                println("real content x: ${getViewPositionX(realcontent)}, y: ${getViewPositionY(realcontent)}")
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> {}
        }
        return super.onTouchEvent(ev)
    }
    ```
    
    4.4 Due to the existence of inertial sliding, we cannot change the position of the second layer layout based on the movement of the finger position in the onTouchEvent. Therefore, in the onTouchEvent of the layout, we only observe the position changes of the layout components, and the final action still needs to be completed in the activity. In the Activity, we use a handler and runnable, and use postDelayed to customize a detection action that is executed once every 1 ms to monitor the position changes of each component in the layout in real time for position adjustment. 
    ```kotlin
    val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                val changedY = getViewPositionY(real_content) - realContentInitPosition
                println("real content $changedY")
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
    ```
    
    4.5 Since we need to handle the position changes of the layout in the Activity, we must first obtain the initial position of the layout and make corresponding position adjustments. Due to the layout initialization in the activity being executed earlier than that in the layout, we use a small delay to obtain the initial position coordinates of the required layout in the Activity. 
    ```kotlin
    val handler1 = Handler()
        val runnable1 = Runnable {
            realContentInitPosition = getViewPositionY(real_content).toFloat()
            toolbar_statusbar_height = toolbar.layoutParams.height + getStatusBarHeight()
        }
        handler1.postDelayed(runnable1,100)
    ```
    In addition, the method for obtaining position coordinates: (The return value is the Y-axis coordinate, and `return position[0]` returns the X-axis coordinate) 
    ```kotlin
    fun getViewPositionY(view: View):Int{
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        return position[1]
    }
    ```
6. If, when handling the touchEvent, it is found that the action is unexpectedly intercepted by the parent control or the action cannot be captured, it is necessary to add `parent.requestDisallowInterceptTouchEvent(true)` in `dispatchTouchEvent()`, `InterceptedTouchEvent()` and `onTouchEvent()`, and it will be OK. Also, if it is found that the content in the lower NestedScrollView cannot be scrolled after using the custom new CoordinatorLayout, just create a new class and write a new NestedScrollView like this. 
```kotlin
class CustomeNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(ev)
    }
}
```

6. The problem of the top title. Since behavior was not used, the code for changing the transparency of the top title was added in the self-established real-time detection loop. According to the positions of the mask layer and the toolbar given by the official Google CoordinatorLayout, the alpha value of the title was adjusted, and that's it. The code is also reflected in 4.4.


#### bug ：
There is a bug in the official Google CoordinatorLayout + AppbarLayout + CollapsingLayout layout. As of my tests so far, it has not been fixed. That is, if the height of the header background is adjusted, it is very easy to slide from the header image when sliding downward. If the finger leaves the screen layout and enters the inertial sliding (fling) stage, and the screen is re-slid (non-header area) before the inertial sliding stops, the layout will shake and be uncontrollable. This is because when sliding starts from the header, this action is handled by the header layout, and the resulting fling is also generated by it. We have no way to stop this fling from the outside. If the screen is touched at this time and the touch point is in the non-header background area, this action will conflict with the previous inertial sliding action. I have checked the solutions and also tried to solve this problem manually, but it did not work. I used the reflection method to obtain the overScroller and flingRunnable objects in the parent's parent's parent class, and manually injected our own scroller using the `set` method in the custom layout. In this way, we can control the inertial sliding action and stop the fling using `abortAnimation()` at any time. If there are experts who have a better solution, please teach me! 

I have checked the solutions and also attempted to solve this problem manually, but it was ineffective. By using the reflection method to obtain the overScroller and flingRunnable objects in the parent's parent's parent class, and manually injecting our own scroller using the `set` method in the custom layout, in this way, we can control the inertial sliding action and stop the fling using `abortAnimation()` at any time. 
