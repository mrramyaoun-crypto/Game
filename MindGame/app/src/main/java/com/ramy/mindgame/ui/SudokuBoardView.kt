package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.view.*
import com.ramy.mindgame.games.SudokuGame
class SudokuBoardView(c:Context,private val g:SudokuGame,private val selected:()->Int,private val select:(Int)->Unit):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var down=-1
 init{isFocusable=true;isClickable=true;contentDescription="Sudoku nine by nine. Tap a square then a number. Notes records possibilities."}
 override fun onDraw(c:Canvas){val w=width/9f;val h=height/9f;val s=selected()
  for(i in 0..80){val x=i%9*w;val y=i/9*h;val related=s>=0&&(i/9==s/9||i%9==s%9||i/27==s/27&&i%9/3==s%9/3||g.board[s]>0&&g.board[i]==g.board[s]);if(i==s||related){p.color=if(i==s)0xCC137F79.toInt()else 0x26A9B190;c.drawRect(x,y,x+w,y+h,p)}
   p.typeface=GameFonts.face(context);p.textAlign=Paint.Align.CENTER
   if(g.board[i]>0){p.textSize=w*.72f;p.color=when{g.conflicts(i,g.board[i])->0xFFAD322C.toInt();i==s->Theme.ink;g.fixed[i]==1->0xFF20180E.toInt();else->0xFF096F6B.toInt()};c.drawText("${g.board[i]}",x+w/2,y+h/2-(p.ascent()+p.descent())/2,p)}
   else if(g.notes[i]!=0){p.textSize=w*.23f;p.color=if(i==s)Theme.ink else 0xFF82714F.toInt();for(n in 1..9)if(g.notes[i] and(1 shl n)!=0){val xx=x+((n-1)%3+.5f)*w/3;val yy=y+((n-1)/3+.5f)*h/3;c.drawText("$n",xx,yy-(p.ascent()+p.descent())/2,p)}}
  };repeat(10){i->p.strokeWidth=if(i%3==0)maxOf(1.8f,width*.005f)else maxOf(.5f,width*.0013f);p.color=if(i%3==0)0xFF4C3520.toInt()else 0x667E6746;c.drawLine(i*w,0f,i*w,height.toFloat(),p);c.drawLine(0f,i*h,width.toFloat(),i*h,p)}
 }
 private fun cell(x:Float,y:Float):Int=if(x>=0&&y>=0&&x<width&&y<height)(y/height*9).toInt()*9+(x/width*9).toInt()else -1
 override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{down=cell(e.x,e.y);parent.requestDisallowInterceptTouchEvent(true)};MotionEvent.ACTION_UP->{val i=cell(e.x,e.y);if(i>=0&&i==down){select(i);invalidate();performClick()};parent.requestDisallowInterceptTouchEvent(false)};MotionEvent.ACTION_CANCEL->parent.requestDisallowInterceptTouchEvent(false)};return true}
 override fun performClick():Boolean{super.performClick();return true}
}
