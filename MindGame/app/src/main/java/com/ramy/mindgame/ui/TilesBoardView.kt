package com.ramy.mindgame.ui
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.*
import com.ramy.mindgame.games.TilesGame
import kotlin.math.abs

/** Native swipe surface with tiles traveling from their true pre-move positions. */
class TilesBoardView(c: Context,private val game: TilesGame,private val motion: ()->Boolean,private val changed: ()->Unit): View(c) {
    private data class Travel(val from:Int,val to:Int,val value:Int)
    private val moving=mutableListOf<Travel>();private var fraction=1f;private var animator:ValueAnimator?=null;private var x=0f;private var y=0f
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private fun tile(c:Canvas,index:Float,row:Float,value:Int){val size=width/4f;val rect=RectF(index*size+3,row*size+3,(index+1)*size-3,(row+1)*size-3)
        if(value==0){paint.color=0x665F4428;c.drawRoundRect(rect,5f,5f,paint);paint.style=Paint.Style.STROKE;paint.color=0x66FFF1C9;paint.strokeWidth=1f;c.drawRoundRect(rect,5f,5f,paint);paint.style=Paint.Style.FILL;return}
        val style=when(value){2->"ivory";4,64,512->"teal";8,16,32,1024->"terra";128,256,2048->"gold";else->"navy"};PhysicalPieces.ceramic(context,c,rect,style)
        paint.typeface=Typeface.create("serif",Typeface.NORMAL);paint.textAlign=Paint.Align.CENTER;paint.textSize=size*if(value>=1024).31f else if(value>=128).40f else .55f;paint.color=if(value==2)Theme.darkInk else Theme.ink
        paint.setShadowLayer(1f,0f,1f,if(value==2)0x33FFFFFF else 0x9943250D.toInt());c.drawText("$value",rect.centerX(),rect.centerY()-(paint.ascent()+paint.descent())/2,paint);paint.clearShadowLayer()
    }
    fun move(direction:Int) {
        if(fraction<1f)return
        val before=game.board.copyOf();if(!game.slide(direction))return
        moving.clear()
        repeat(4){line->val ids=IntArray(4){p->when(direction){0->line*4+p;1->p*4+line;2->line*4+3-p;else->(3-p)*4+line}};val values=ids.filter{before[it]!=0};var k=0;var target=0
            while(k<values.size){val from=values[k];moving.add(Travel(from,ids[target],before[from]));if(k+1<values.size&&before[from]==before[values[k+1]]){moving.add(Travel(values[k+1],ids[target],before[values[k+1]]));k+=2}else k++;target++}}
        changed()
        if(motion()){fraction=0f;animator=ValueAnimator.ofFloat(0f,1f).apply{duration=180;addUpdateListener{fraction=it.animatedValue as Float;invalidate()};start()}}else {fraction=1f;invalidate()}
    }
    fun sync(){animator?.cancel();fraction=1f;invalidate()}
    init{isFocusable=true;isClickable=true;contentDescription="2048 board. Swipe left, up, right or down to move and merge tiles."}
    override fun onDraw(c:Canvas){repeat(16){tile(c,(it%4).toFloat(),(it/4).toFloat(),0)}
        if(fraction>=1f)game.board.indices.filter{game.board[it]>0}.forEach{tile(c,(it%4).toFloat(),(it/4).toFloat(),game.board[it])}
        else moving.forEach{t->val px=t.from%4+(t.to%4-t.from%4)*fraction;val py=t.from/4+(t.to/4-t.from/4)*fraction;tile(c,px,py,if(fraction>.9f)game.board[t.to]else t.value)}
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{x=e.x;y=e.y;parent.requestDisallowInterceptTouchEvent(true)};MotionEvent.ACTION_UP->{val dx=e.x-x;val dy=e.y-y;if(maxOf(abs(dx),abs(dy))>20*resources.displayMetrics.density)move(if(abs(dx)>abs(dy))if(dx>0)2 else 0 else if(dy>0)3 else 1);parent.requestDisallowInterceptTouchEvent(false);performClick()};MotionEvent.ACTION_CANCEL->parent.requestDisallowInterceptTouchEvent(false)};return true}
    override fun onDetachedFromWindow(){animator?.cancel();super.onDetachedFromWindow()}
    override fun performClick():Boolean{super.performClick();return true}
}
