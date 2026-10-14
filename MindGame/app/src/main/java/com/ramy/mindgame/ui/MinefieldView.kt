package com.ramy.mindgame.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.*
import com.ramy.mindgame.games.MinesGame
import kotlin.math.*

/** Pan/zoom minefield with normal-size hit targets on large boards. */
class MinefieldView(context: Context,private val game: MinesGame,private val flagMode: ()->Boolean,private val changed: ()->Unit): View(context) {
    var viewChanged: (() -> Unit)?=null
    fun viewport()=android.graphics.RectF((-offsetX/(tile*zoom)/game.columns).coerceIn(0f,1f),(-offsetY/(tile*zoom)/game.rows).coerceIn(0f,1f),((width-offsetX)/(tile*zoom)/game.columns).coerceIn(0f,1f),((height-offsetY)/(tile*zoom)/game.rows).coerceIn(0f,1f))
    private var tile=44f*resources.displayMetrics.density
    private var zoom=1f
    private var offsetX=0f;private var offsetY=0f
    private var downX=0f;private var downY=0f;private var lastX=0f;private var lastY=0f
    private var dragged=false;private var scaling=false;private var initialized=false
    private var lastTap=-1
    private val scaleDetector=ScaleGestureDetector(context,object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(d: ScaleGestureDetector): Boolean { scaling=true;return true }
        override fun onScale(d: ScaleGestureDetector): Boolean { scaleTo(zoom*d.scaleFactor,d.focusX,d.focusY);return true }
    })
    private val gestures=GestureDetector(context,object: GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: android.view.MotionEvent)=true
        override fun onLongPress(e: android.view.MotionEvent) { if(!dragged && !scaling) { val i=cellAt(e.x,e.y);if(i>=0) {game.flag(i);performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);changed();invalidate();viewChanged?.invoke();lastTap=-1;dragged=true} } }
    })
    init { isClickable=true;isFocusable=true;contentDescription="Minefield ${game.columns} columns by ${game.rows} rows, ${game.mineCount} mines. Drag to pan, pinch to zoom. Tap to reveal; long press to flag." }
    override fun onSizeChanged(w: Int,h: Int,oldw: Int,oldh: Int) { if(!initialized) { initialized=true;tile=w.toFloat()/if(game.columns<=9)game.columns else 8;offsetX=0f;offsetY=0f };clamp() }
    private fun clamp() { val ww=game.columns*tile*zoom;val hh=game.rows*tile*zoom;offsetX=if(ww<=width) (width-ww)/2 else offsetX.coerceIn(width-ww,0f);offsetY=if(hh<=height) (height-hh)/2 else offsetY.coerceIn(height-hh,0f) }
    private fun scaleTo(value: Float,x: Float,y: Float) { val next=value.coerceIn(.45f,2.5f);val ratio=next/zoom;offsetX=x-(x-offsetX)*ratio;offsetY=y-(y-offsetY)*ratio;zoom=next;clamp();invalidate();viewChanged?.invoke() }
    fun zoomBy(f: Float) {scaleTo(zoom*f,width/2f,height/2f)}
    private fun cellAt(x: Float,y: Float): Int { val col=floor((x-offsetX)/(tile*zoom)).toInt();val row=floor((y-offsetY)/(tile*zoom)).toInt();return if(col in 0 until game.columns && row in 0 until game.rows) row*game.columns+col else -1 }
    override fun onDraw(c: Canvas) {
        super.onDraw(c);c.save();c.clipRect(0,0,width,height);ArcadeArt.round(c,0f,0f,width.toFloat(),height.toFloat(),20f,Color.parseColor("#D5BF96"));c.translate(offsetX,offsetY);c.scale(zoom,zoom)
        for(i in game.mines.indices) {val x=(i%game.columns)*tile;val y=(i/game.columns)*tile;if(x*zoom+offsetX>width || y*zoom+offsetY>height || (x+tile)*zoom+offsetX<0 || (y+tile)*zoom+offsetY<0)continue
            val covered=game.open[i]==0;val v=when { game.lost && game.mines[i]==1 -> -2;game.flags[i]==1 -> -1;!covered->game.adjacent(i);else->0 }
            val piece=PieceDrawable("mines",v,covered && !game.lost);piece.setBounds(x.toInt(),y.toInt(),(x+tile).toInt(),(y+tile).toInt());piece.draw(c)
        };c.restore()
    }
    override fun onTouchEvent(e: android.view.MotionEvent): Boolean {
        scaleDetector.onTouchEvent(e);gestures.onTouchEvent(e)
        when(e.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> { parent.requestDisallowInterceptTouchEvent(true);downX=e.x;downY=e.y;lastX=e.x;lastY=e.y;dragged=false;scaling=false;lastTap=cellAt(e.x,e.y) }
            android.view.MotionEvent.ACTION_MOVE -> { if(!scaleDetector.isInProgress) { if(hypot(e.x-downX,e.y-downY)>resources.displayMetrics.density*8)dragged=true;if(dragged) {offsetX+=e.x-lastX;offsetY+=e.y-lastY;clamp();invalidate();viewChanged?.invoke()};lastX=e.x;lastY=e.y } }
            android.view.MotionEvent.ACTION_UP -> { if(!dragged && !scaling && lastTap>=0) { if(flagMode())game.flag(lastTap) else if(game.open[lastTap]==1)game.chord(lastTap) else game.reveal(lastTap);changed();invalidate();viewChanged?.invoke();performClick() };parent.requestDisallowInterceptTouchEvent(false) }
            android.view.MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        };return true
    }
    override fun performClick(): Boolean {super.performClick();return true}
}
