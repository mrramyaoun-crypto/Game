package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.Canvas
import android.view.View
class FeedbackPins(context: Context,private val exact: Int,private val misplaced: Int): View(context) {
    override fun onDraw(c: Canvas) { super.onDraw(c);val a=ArcadeArt; val scale=minOf(width/48f,height/44f);c.save();c.scale(scale,scale);repeat(4) { i ->a.peg(c,15f+(i%2)*17,13f+(i/2)*17,5f,when { i<exact->a.color("#171510");i<exact+misplaced->a.color("#F9EED9");else->a.color("#473320") }) };c.restore() }
}
