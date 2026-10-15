package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.view.View
class HuntProgress(c:Context,private val progress:()->Float):View(c){private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){val r=RectF(1f,1f,width-1f,height-1f);p.color=0xAA1D1308.toInt();c.drawRoundRect(r,height/2f,height/2f,p);p.color=Theme.brass;p.style=Paint.Style.STROKE;p.strokeWidth=1f;c.drawRoundRect(r,height/2f,height/2f,p);p.style=Paint.Style.FILL;p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(0xFFF4D274.toInt(),0xFFB88124.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(2f,2f,2+(width-4)*progress().coerceIn(0f,1f),height-2f,height/2f,height/2f,p);p.shader=null}
}
class TileMilestones(c:Context,private val highest:()->Int):View(c){private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){val sx=width/300f;p.color=Theme.brass;p.strokeWidth=4f*sx;c.drawLine(20*sx,12*sx,280*sx,12*sx,p);intArrayOf(512,1024,2048,4096).forEachIndexed{i,n->val x=(20+i*86.7f)*sx;PhysicalPieces.socket(c,x,12*sx,9*sx);if(highest()>=n)PhysicalPieces.peg(c,x,12*sx,7*sx,0xFF168F88.toInt());p.color=Theme.ink;p.typeface=GameFonts.face(context);p.textAlign=Paint.Align.CENTER;p.textSize=13f*sx;c.drawText("$n",x,36*sx,p)}}
}
