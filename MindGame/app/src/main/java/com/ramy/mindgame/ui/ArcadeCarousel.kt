package com.ramy.mindgame.ui
import android.content.Context
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import kotlin.math.roundToInt

class ArcadeCarousel(context: Context): HorizontalScrollView(context) {
    var stride=1
    init { isHorizontalScrollBarEnabled=false; overScrollMode=OVER_SCROLL_NEVER }
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val result=super.onTouchEvent(ev)
        if(ev.action==MotionEvent.ACTION_UP || ev.action==MotionEvent.ACTION_CANCEL) postDelayed({ smoothScrollTo((scrollX.toFloat()/stride).roundToInt()*stride,0) },180)
        return result
    }
}
