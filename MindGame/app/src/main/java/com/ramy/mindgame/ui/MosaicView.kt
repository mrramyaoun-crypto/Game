package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.view.*
import com.ramy.mindgame.games.*
import kotlin.math.*

fun mosaicColor(r: Int,c: Int,n: Int): Int = intArrayOf(0xFF296E70.toInt(),0xFF3C9695.toInt(),0xFFD3A344.toInt(),0xFFC37441.toInt(),0xFF214B54.toInt())[((r/2+c/3)+(if(c==n/2||r==n/2)2 else 0))%5]
class MosaicPreview(c: Context,private val picture: MosaicPicture,private val revealed: Boolean): View(c) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c: Canvas) {val side=minOf(width,height).toFloat();val s=(side-12)/picture.size;val x=(width-side)/2f+6;val y=(height-side)/2f+6;p.color=Theme.darkInk;c.drawRect(x-4,y-4,x+side-8,y+side-8,p);for(i in picture.mask.indices){p.color=if(revealed&&picture.mask[i]=='1')mosaicColor(i/picture.size,i%picture.size,picture.size) else if(revealed)0xFFE4CE9E.toInt() else 0xFF8A7660.toInt();val xx=x+i%picture.size*s;val yy=y+i/picture.size*s;c.drawRect(xx+.5f,yy+.5f,xx+s-.5f,yy+s-.5f,p)};if(!revealed){p.color=Theme.ink;p.textSize=24f;p.textAlign=Paint.Align.CENTER;c.drawText("?",width/2f,height/2f+8,p)};contentDescription=if(revealed)picture.name else "Unrestored picture"}
}
class MosaicView(c: Context,private val g: NonogramGame,private val cross: ()->Boolean,private val pan: ()->Boolean,private val changed: ()->Unit): View(c) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val d=resources.displayMetrics.density
    private var tile=24*d;private var left=75*d;private var top=90*d;private var zoom=1f;private var ox=0f;private var oy=0f
    private var downX=0f;private var downY=0f;private var lastX=0f;private var lastY=0f;private var drawing=false;private var scaling=false;private var longHandled=false;private var before:IntArray?=null;private var value=1;private var last=-1
    private val scale=ScaleGestureDetector(c,object: ScaleGestureDetector.SimpleOnScaleGestureListener(){override fun onScaleBegin(d: ScaleGestureDetector): Boolean {scaling=true;return true};override fun onScale(det: ScaleGestureDetector): Boolean {val z=(zoom*det.scaleFactor).coerceIn(.7f,3f);ox=det.focusX-(det.focusX-ox)*z/zoom;oy=det.focusY-(det.focusY-oy)*z/zoom;zoom=z;clamp();invalidate();return true}})
    private val gesture=GestureDetector(c,object: GestureDetector.SimpleOnGestureListener(){override fun onDown(e: MotionEvent)=true;override fun onLongPress(e: MotionEvent){if(!drawing&&!scaling&&!pan()){val i=cell(e.x,e.y);if(i>=0){g.mark(i,true);changed();invalidate();longHandled=true;performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)}}}})
    init {isFocusable=true;isClickable=true;contentDescription="${g.size} by ${g.size} mosaic. Row and column clues; drag to fill, Mark crosses empty cells, Pan explores, pinch zooms."}
    override fun onSizeChanged(w: Int,h: Int,oldw: Int,oldh: Int){tile=(w-left)/minOf(g.size,12);top=maxOf(55*d,(0 until g.size).maxOf{g.col(it).size}*14*d+12*d);clamp()}
    private fun clamp(){val ww=(left+g.size*tile)*zoom;val hh=(top+g.size*tile)*zoom;ox=ox.coerceIn(minOf(0f,width-ww),0f);oy=oy.coerceIn(minOf(0f,height-hh),0f)}
    fun zoomBy(f: Float){val z=(zoom*f).coerceIn(.7f,3f);ox=width/2f-(width/2f-ox)*z/zoom;oy=height/2f-(height/2f-oy)*z/zoom;zoom=z;clamp();invalidate()}
    private fun cell(x: Float,y: Float): Int {val col=floor(((x-ox)/zoom-left)/tile).toInt();val r=floor(((y-oy)/zoom-top)/tile).toInt();return if(col in 0 until g.size&&r in 0 until g.size)r*g.size+col else -1}
    private fun paint(i: Int){if(i<0||g.won)return;if(before==null){before=g.beginStroke();value=if(g.board[i]==(if(cross())2 else 1))0 else if(cross())2 else 1};g.paint(i,value);last=i;invalidate()}
    override fun onDraw(c: Canvas){c.save();c.clipRect(0,0,width,height);p.color=0xFFF0DEB9.toInt();c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);c.translate(ox,oy);c.scale(zoom,zoom)
        p.typeface=Typeface.create("sans-serif",Typeface.BOLD);p.textSize=minOf(14*d,tile*.48f);p.textAlign=Paint.Align.CENTER
        repeat(g.size){col->p.color=if(g.colDone(col))0xFF9B9B7D.toInt() else Theme.darkInk;val clues=g.col(col);clues.forEachIndexed{k,v->c.drawText("$v",left+col*tile+tile/2,top-7*d-(clues.size-k-1)*14*d,p)}}
        repeat(g.size){r->p.color=if(g.rowDone(r))0xFF9B9B7D.toInt() else Theme.darkInk;p.textAlign=Paint.Align.RIGHT;g.row(r).asReversed().forEachIndexed{k,v->c.drawText("$v",left-8*d-k*18*d,top+r*tile+tile*.65f,p)}}
        for(i in g.board.indices){val x=left+i%g.size*tile;val y=top+i/g.size*tile;p.color=if(g.board[i]==1)mosaicColor(i/g.size,i%g.size,g.size) else 0xFFEDDCB6.toInt();c.drawRect(x+1,y+1,x+tile-1,y+tile-1,p);if(g.board[i]==1){p.color=0x44FFFFFF;p.strokeWidth=1.4f;c.drawLine(x+4,y+3,x+tile-4,y+3,p)};if(g.board[i]==2){p.color=0xFF8F8970.toInt();p.strokeWidth=1.5f*d;c.drawLine(x+tile*.35f,y+tile*.35f,x+tile*.65f,y+tile*.65f,p);c.drawLine(x+tile*.65f,y+tile*.35f,x+tile*.35f,y+tile*.65f,p)}}
        repeat(g.size+1){k->p.color=if(k%5==0)0xFF765832.toInt() else 0xFFBBA57B.toInt();p.strokeWidth=if(k%5==0)1.5f*d else .6f*d;c.drawLine(left+k*tile,top,left+k*tile,top+g.size*tile,p);c.drawLine(left,top+k*tile,left+g.size*tile,top+k*tile,p)};c.restore()}
    override fun onTouchEvent(e: MotionEvent): Boolean {scale.onTouchEvent(e);gesture.onTouchEvent(e);when(e.actionMasked){MotionEvent.ACTION_DOWN->{parent.requestDisallowInterceptTouchEvent(true);downX=e.x;downY=e.y;lastX=e.x;lastY=e.y;drawing=false;scaling=false;longHandled=false;before=null;last=-1}
        MotionEvent.ACTION_MOVE->{if(!scaling&&!longHandled){if(pan()||cell(downX,downY)<0){ox+=e.x-lastX;oy+=e.y-lastY;clamp();invalidate()}else if(hypot(e.x-downX,e.y-downY)>5*d){drawing=true;val start=if(last<0)cell(downX,downY)else last;val end=cell(e.x,e.y);if(start>=0&&end>=0){val a=start/g.size;val b=start%g.size;val dr=end/g.size-a;val dc=end%g.size-b;val steps=maxOf(abs(dr),abs(dc));for(k in 0..steps){val r=a+if(steps==0)0 else (dr*k.toFloat()/steps).roundToInt();val col=b+if(steps==0)0 else (dc*k.toFloat()/steps).roundToInt();paint(r*g.size+col)}}};lastX=e.x;lastY=e.y}}
        MotionEvent.ACTION_UP->{if(!scaling&&!longHandled&&!pan()&&!drawing)paint(cell(e.x,e.y));before?.let{g.endStroke(it);changed()};parent.requestDisallowInterceptTouchEvent(false);invalidate();performClick()}
        MotionEvent.ACTION_CANCEL->{before?.let{g.endStroke(it);changed()};parent.requestDisallowInterceptTouchEvent(false)} };return true}
    override fun performClick():Boolean {super.performClick();return true}
}
