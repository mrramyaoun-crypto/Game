package com.ramy.mindgame.ui
import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.*

object WinBurst {
    fun show(host: View): ValueAnimator {
        val burst=object: Drawable() {
            val p=Paint(Paint.ANTI_ALIAS_FLAG)
            var t=0f
            val colors=listOf("#F6D78A","#89D5B8","#F28C9C","#A990E2","#8BC8EF").map(Color::parseColor)
            override fun draw(c: Canvas) {
                val d=host.resources.displayMetrics.density
                repeat(48) { i ->
                    val angle=(i*2.399963).toFloat();val speed=(120+(i%7)*22)*d
                    val x=bounds.width()/2f+cos(angle)*speed*t
                    val y=bounds.height()*.4f+sin(angle)*speed*t-170*d*t+310*d*t*t
                    p.color=colors[i%colors.size];p.alpha=((1-t)*255).toInt().coerceIn(0,255)
                    c.save();c.translate(x,y);c.rotate(i*23f+t*260)
                    if(i%3==0) { val path=Path();path.moveTo(0f,-5*d);path.lineTo(2*d,-2*d);path.lineTo(6*d,0f);path.lineTo(2*d,2*d);path.lineTo(0f,6*d);path.lineTo(-2*d,2*d);path.lineTo(-5*d,0f);path.lineTo(-2*d,-2*d);path.close();c.drawPath(path,p) } else c.drawRoundRect(-2*d,-5*d,2*d,5*d,d,d,p)
                    c.restore()
                }
            }
            override fun setAlpha(alpha: Int) {};override fun setColorFilter(cf: ColorFilter?) {};override fun getOpacity()=PixelFormat.TRANSLUCENT
        }
        burst.setBounds(0,0,host.width,host.height);host.overlay.add(burst)
        return ValueAnimator.ofFloat(0f,1f).apply {duration=1300;addUpdateListener {burst.t=it.animatedValue as Float;burst.invalidateSelf()};addListener(object: AnimatorListenerAdapter(){override fun onAnimationEnd(animation: Animator){host.overlay.remove(burst)}});start()}
    }
}
