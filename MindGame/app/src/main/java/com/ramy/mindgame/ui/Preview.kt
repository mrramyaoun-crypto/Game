package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.sin

/** Illustrated, moving miniature boards, sharing artwork with their games. */
class Preview(context: Context,private val id: String,private val accent: Int,private val moving: ()->Boolean): View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private fun piece(c: Canvas,type: String,v: Int,x: Float,y: Float,size: Float,raised: Boolean=true) { c.save();c.translate(x,y);val d=PieceDrawable(type,v,raised);d.setBounds(0,0,size.toInt(),size.toInt());d.draw(c);c.restore() }
    override fun onDraw(c: Canvas) {
        super.onDraw(c);val a=ArcadeArt;val scale=minOf(width/300f,height/210f);c.save();c.translate((width-300*scale)/2,(height-210*scale)/2);c.scale(scale,scale)
        val t=android.os.SystemClock.uptimeMillis()/1100.0;val bob=if(moving()) sin(t).toFloat()*3f else 0f
        paint.shader=RadialGradient(150f,130f,130f,Color.argb(42,Color.red(accent),Color.green(accent),Color.blue(accent)),Color.TRANSPARENT,Shader.TileMode.CLAMP);c.drawCircle(150f,110f,130f,paint);paint.shader=null
        c.translate(0f,bob);c.save();c.rotate(-5f,150f,105f)
        when(id) {
            "enigma" -> {
                a.round(c,25f,27f,250f,163f,15f,a.color("#281C1B"));a.round(c,25f,19f,250f,163f,15f,a.color("#5D3B29"));repeat(10) { r ->a.line(c,35f,32f+r*14,264f,32f+r*14,a.color("#69442F"),1f) };a.round(c,40f,32f,220f,88f,9f,a.color("#DAC08A"));
                repeat(3) { r ->repeat(4) { k ->a.text(c,"${(k*3+r*2)%10}",65f+k*32,58f+r*24,20f,a.color("#54402F")) };a.line(c,192f,53f+r*24,240f,53f+r*24,a.color("#AB8C56"),2f) };a.line(c,44f,82f,128f,78f,a.color("#A24739"),2f)
                repeat(7) { k ->a.peg(c,54f+k*32,147f,12f,a.color("#E3C797"));a.text(c,"${k+1}",54f+k*32,151f,13f,a.color("#4C362A")) };repeat(4) { k ->a.circle(c,38f+224*(k%2),30f+142*(k/2),3f,a.color("#C29960")) }
            }
            "memory" -> { val pos=listOf(Triple(0,65f,26f),Triple(1,139f,17f),Triple(2,60f,108f),Triple(0,143f,103f));pos.forEachIndexed { i,p ->c.save();c.rotate(if(i%2==0) -8f else 9f,p.second+38,p.third+38);piece(c,if(i==2) "memory-back" else "fruit",p.first,p.second,p.third,78f);c.restore() } }
            "mines" -> { a.round(c,49f,13f,200f,185f,16f,a.color("#1B3149"));repeat(16) { i ->piece(c,"mines",when(i) {6->-1;5->2;9->1;else->0},62f+i%4*44,24f+i/4*41,40f,raised=i !in listOf(0,1,4,5,9)) } }
            "mastermind" -> { a.round(c,36f,19f,224f,171f,17f,a.color("#1D2D43"));val colors=listOf("#F37B87","#A2DCC3","#BEA0E7","#F8CE78").map(Color::parseColor);repeat(3) { r ->repeat(4) { k ->a.peg(c,67f+k*44,53f+r*50,16f,colors[(k+r)%4]) };repeat(4) { k ->a.peg(c,226f+(k%2)*12,46f+r*50+(k/2)*12,3.5f,if(k<=r) a.color("#F17E8C") else a.color("#DFD4BC")) } } }
            "2048" -> { a.round(c,49f,8f,202f,192f,18f,a.color("#1C3147"));val v=listOf(2,4,0,0,4,8,0,0,8,16,32,0,0,0,64,128);repeat(16) { i ->piece(c,"tile",v[i],60f+i%4*46,18f+i/4*44,43f) } }
            "sudoku" -> { a.round(c,55f,13f,190f,184f,9f,a.color("#AA937A"));a.round(c,55f,8f,190f,182f,9f,a.color("#FFF3DB"));repeat(9) { i ->a.round(c,65f+(i%3)*57,18f+(i/3)*54,52f,49f,3f,if(i==4) a.color("#ECD2A3") else a.color("#F7EAD1"));a.text(c,listOf("1","8","7","4","9","2","3","6","5")[i],91f+(i%3)*57,53f+(i/3)*54,29f,a.color("#5B685D")) };a.line(c,83f,152f,198f,43f,a.color("#7AABB0"),2.5f) }
            else -> { a.round(c,58f,12f,184f,184f,16f,a.color("#273C39"));repeat(25) { i ->piece(c,"pixel",if("0101011111111110111000100"[i]=='1') 1 else 0,66f+i%5*34,20f+i/5*33,31f) } }
        }
        c.restore();if(moving()) { repeat(4) { i ->val x=28f+i*79;val y=25f+(i%2)*158;val r=2f+sin(t+i).toFloat();a.circle(c,x,y,r,accent) } };c.restore()
        if(moving() && isAttachedToWindow)postInvalidateDelayed(40)
    }
}
