# Multi-Layer-Parallax-Background
## 前言
因为公司在做电影院线的手机应用，有一个需求是做如图的这种多层视差头部背景(multi-layer parallax background)，原生且不使用第三方库。所以首先想到的就是直接使用谷歌官方的CoordinatorLayout + AppbarLayout + CollapsingLayout来实现最基础的视差背景效果。

平台：Android Studio, 语言：Kotlin

最终效果如图，经反复测试流畅无问题（如果后续测试有问题还会更新）。
![demo演示](https://user-gold-cdn.xitu.io/2019/5/15/16abc122a1127d62?w=320&h=564&f=gif&s=5220537)
## 想法
1. 首先使用谷歌官方的CoordinatorLayout + AppbarLayout + CollapsingLayout布局来实现一个基本的带折叠效果的布局，可以自定义背景图片的大小和布局方式，想实现parallax只要添加“parallax” 属性就可以轻松实现。

2. 然后就是如何添加另外多出来的这一层背景布局并给它不一样的移动速度，让我们看起来是有三层（背景+背景层内容+下方具体内容）layout带有三个不同的移动速度，造成多层视差效果。
3. 背景层添加内容很容易，只要新建一个新的LinearLayout,构建好内容的布局，在AppbarLayout中include进来就可以了。因为这一部分的源码本质实际上是extend了一个FrameLayout，所以我们可以将多层内容重叠摆放在头部位置。然后在MainActivity中获取内容的id，这一步算完成了。
4. 下面是最重要的一步，如何让这一部分的layout有不一样的上划速度，并且在惯性滑动过程中，也可以随时监测底部位置并更改自己本身的位置。

    4.1.所有的触摸事件都绕不开三个大佬，`dispatchTouchEvent()`,`InterceptedTouchEvent()` 和 `onTouchEvent()`。所以果断重写CoordinatorLayout, 重写 `InterceptedTouchEvent()` 和 `onTouchEvent()`，`dispatchTouchEvent()` 暂时不用管他。我们在`InterceptedTouchEvent()`中截获手指在屏幕上的动作，然后根据我们的要求来分发事件。如果检测到手指是向上划的，就`return true`把事件传递给`onTouchEvent()`去处理。
    
    4.2 在新的CoordinatorLayout中，还要写一个open function来使Acticity可以将头部背景的图片传递过来，只有这样我们才能正常在新建的layout中处理图片位置和获取相关信息。这一点很重要，否则我们没法在这个文件里找到背景图片的代码位置（没法findViewById）。
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
    
    4.3 在`onTouchEvent()`中，实时检测底部的位置变化。这就需要我们在4.2所定义的方法中将三层内容的信息全部传递过来，方便我们在layout中检测和更改。
    
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
    
    4.4 因为有惯性滑动的存在，我们不能在onTouchEvent中根据手指位置的移动来改变第二层layout的位置，所以在layout的onTouchEvent中我们只观察布局原件们的位置变化，最终的动作还是要在activity中完成。在Activity中，我们用一个handler和runneble，使用postDelayed来自定义一个每1ms执行一次的检测动作，来实时监测layout中各个原件的位置变化，来进行位置调整。
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
    
    4.5 既然在Activity中要处理布局的位置变化，我们就要先获取布局的初始位置并做出相应的位置调整,由于activity中的布局初始化比layout中的布局初始化要早执行，所以我们通过一个小的延时来在Activity中获取到所需的layout的初始位置坐标。
    ```kotlin
    val handler1 = Handler()
        val runnable1 = Runnable {
            realContentInitPosition = getViewPositionY(real_content).toFloat()
            toolbar_statusbar_height = toolbar.layoutParams.height + getStatusBarHeight()
        }
        handler1.postDelayed(runnable1,100)
    ```
    另外，获取位置坐标的方法：(返回值即为Y轴坐标，`return position[0]`即返回x轴坐标)
    ```kotlin
    fun getViewPositionY(view: View):Int{
        val position = IntArray(2)
        view.getLocationOnScreen(position)
        return position[1]
    }
    ```
5. 如果在处理touchEvent的时候，发现动作意外的被父控件拦截或者捕捉不到动作了，一定要在`dispatchTouchEvent()`,`InterceptedTouchEvent()` 和 `onTouchEvent()` 中加上` parent.requestDisallowInterceptTouchEvent(true)`就OK了。还有如果发现在使用了自定义的新CoordinatorLayout之后，下部的NestedScrollView中的内容无法滑动了，再新建一个class然后像这样写一个新的NestedScrollView就行了。
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

6. 顶部标题的问题，由于没有使用behavior，所以还是在自己建立的实时检测循环里加入了改变顶部title透明度的代码，根据谷歌官方CoordinatorLayout给的出现遮罩层的位置和出现toolbar的位置，来调整title的alpha值，就OK了。代码也在4.4中有体现。

## 总结
不知道自己使用的postDelayed方法来一直不停的检测位置变化的方法是不是正确，是否会造成对软件运行流畅度的影响。如果各位有建议请提给我谢谢！

传送门：[Github -- Multi-Layer-Parallax-Background](https://github.com/ZhaojiaLii/Multi-Layer-Parallax-Background)


#### bug ：
谷歌官方的CoordinatorLayout + AppbarLayout + CollapsingLayout 布局有一个bug，至今据我测试还没有修复，就是如果在调整了头部背景的高度的时候，很容易在向下滑动的时候从头部图片滑动，如果手指离开屏幕布局进入惯性滑动fling阶段，在惯性滑动没有停止之前重新滑动屏幕（非头部区域），布局会产生抖动而且无法控制。这是因为当开始从头部滑动时，该动作被头部layout处理，产生的fling也是由它产生的，我们没有办法从外部停止这个fling，如果在这个时候触摸屏幕而且触摸点在非头部背景区域，这个动作就会和之前的惯性滑动动作冲突。

查过解决方案，也尝试过手动解决这个问题但是并没有奏效。用反射的方法获取父类的父类的父类中的overScroller和flingRunnable对象，在自定义的layout中用`set`方法手动注入我们自己的scroller，这样我们就可以控制惯性滑动的动作并随时使用`abortAnimation()`停止fling。如果有大神有更好的办法请赐教！
