package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

object Theme {
    val bg=Color.parseColor("#352215"); val surface=Color.parseColor("#EBD6AF"); val ink=Color.parseColor("#FFF0CE"); val muted=Color.parseColor("#D3BA8F"); val mint=Color.parseColor("#7CD0C6"); val darkInk=Color.parseColor("#39291A");val brass=Color.parseColor("#D1A451")
    fun dp(c: Context,v: Int)=(v*c.resources.displayMetrics.density).toInt()
    fun shape(color: Int,radius: Float=18f,stroke: Int=0)=GradientDrawable().apply { setColor(color); cornerRadius=radius; if(stroke!=0) setStroke(2,stroke) }
    fun text(c: Context,s: String,size: Float=16f,color: Int=ink,bold: Boolean=false)=TextView(c).apply { text=s; textSize=size; setTextColor(color); typeface=GameFonts.face(c,bold); setPadding(0,dp(c,4),0,dp(c,4)) }
    fun column(c: Context)=LinearLayout(c).apply { orientation=LinearLayout.VERTICAL }
    fun row(c: Context)=LinearLayout(c).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
    fun button(c: Context,s: String,action: ()->Unit)=Button(c).apply { text=s; isAllCaps=false; textSize=14f;typeface=GameFonts.face(c); setTextColor(ink); background=android.graphics.drawable.StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_pressed),shape(0xFF4B8077.toInt(),dp(c,12).toFloat(),brass)); addState(intArrayOf(),GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xFF265E58.toInt(),0xFF123F3B.toInt())).apply { cornerRadius=dp(c,12).toFloat();setStroke(dp(c,1),brass) }) }; minHeight=dp(c,48); minimumHeight=dp(c,48); setPadding(dp(c,8),0,dp(c,8),0); setOnClickListener { action() } }
    fun paper(c: Context)=MaterialDrawable(c,true)
    fun space(c: Context,h: Int)=View(c).apply { layoutParams=LinearLayout.LayoutParams(1,dp(c,h)) }
    fun addWeighted(row: LinearLayout,v: View,height: Int=48) { row.addView(v,LinearLayout.LayoutParams(0,dp(row.context,height),1f).apply { setMargins(dp(row.context,3),dp(row.context,3),dp(row.context,3),dp(row.context,3)) }) }
}
