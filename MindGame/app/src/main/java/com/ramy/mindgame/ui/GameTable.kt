package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.*
import kotlin.math.*

/** Physical artwork underneath live, accessible native game controls. Artwork has no game state. */
class GameTable(c:Context,val gameId:String,var designHeight:Float=786f):ViewGroup(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG);private val regions=mutableMapOf<View,RectF>();private val art=ArtAssets.bitmap(c,"game_${gameId}_v05")
 init{setWillNotDraw(false);clipChildren=false;importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
 fun place(v:View,x:Float,y:Float,w:Float,h:Float):View{regions[v]=RectF(x,y,x+w,y+h);addView(v);return v}
 fun text(value:()->String,x:Float,y:Float,w:Float,h:Float,size:Float=16f,color:Int=Theme.ink,bold:Boolean=false,align:Paint.Align=Paint.Align.CENTER):GameText{val v=GameText(context,value,size,color,bold,align);place(v,x,y,w,h);return v}
 fun control(label:String,icon:String?,x:Float,y:Float,w:Float,h:Float,action:()->Unit):GameControl{val v=GameControl(context,label,icon,action);place(v,x,y,w,h);return v}
 fun refresh(){invalidate();for(i in 0 until childCount)getChildAt(i).invalidate()}
 override fun onMeasure(ws:Int,hs:Int){val w=MeasureSpec.getSize(ws);val s=w/393f;setMeasuredDimension(w,(designHeight*s).roundToInt());regions.forEach{(v,r)->v.measure(MeasureSpec.makeMeasureSpec((r.width()*s).roundToInt(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec((r.height()*s).roundToInt(),MeasureSpec.EXACTLY))}}
 override fun onLayout(changed:Boolean,l:Int,t:Int,r:Int,b:Int){val s=width/393f;regions.forEach{(v,q)->v.layout((q.left*s).roundToInt(),(q.top*s).roundToInt(),(q.right*s).roundToInt(),(q.bottom*s).roundToInt())}}
 override fun onDraw(c:Canvas){p.color=Theme.bg;c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);art?.let{c.drawBitmap(it,null,RectF(0f,0f,width.toFloat(),height.toFloat()),p)}}
}
class GameText(c:Context,private val value:()->String,private val size:Float,private val color:Int,private val bold:Boolean,private val align:Paint.Align):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){val s=resources.displayMetrics.widthPixels/393f;p.color=color;p.typeface=Typeface.create("serif",if(bold)Typeface.BOLD else Typeface.NORMAL);p.textSize=size*s;p.textAlign=align;val txt=value();contentDescription=txt;if(p.measureText(txt)>width-4*s)p.textSize*=((width-4*s)/p.measureText(txt)).coerceAtMost(1f);c.drawText(txt,when(align){Paint.Align.LEFT->2*s;Paint.Align.RIGHT->width-2*s;else->width/2f},height/2f-(p.ascent()+p.descent())/2,p)}
}
class GameControl(c:Context,val label:String,private val icon:String?,action:()->Unit):View(c){
 var selectedControl=false;var showLabel=icon==null;var ink=Theme.ink;var drawBody=false;var bodyTeal=false
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 init{isClickable=true;isFocusable=true;contentDescription=label;setOnClickListener{action()}}
 override fun drawableStateChanged(){super.drawableStateChanged();invalidate()}
 override fun getAccessibilityClassName():CharSequence="android.widget.Button"
 override fun onDraw(c:Canvas){val s=minOf(width/48f,height/44f)
  if(drawBody){val r=RectF(2f,3f,width-2f,height-3f);val rad=if(width<height*1.4f)height/2f else 14*s;p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(if(bodyTeal)0xFF1C7972.toInt()else 0xFF76552B.toInt(),if(bodyTeal)0xFF073F3D.toInt()else 0xFF271B10.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(r,rad,rad,p);p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=1.6f*s;p.color=Theme.brass;c.drawRoundRect(r,rad,rad,p);p.style=Paint.Style.FILL}
  if(isPressed||selectedControl){p.color=if(isPressed)0x44FFE5A0 else 0x25FFE5A0;c.drawRoundRect(3f,3f,width-3f,height-3f,minOf(height/2f,12*s),minOf(height/2f,12*s),p)}
  if(icon!=null){c.save();val side=minOf(width*.6f,height*.63f);c.translate(if(showLabel)side*.1f else(width-side)/2,(height-side)/2);c.scale(side/100,side/100);GameIcons.draw(c,icon,ink);c.restore()}
  if(showLabel){p.color=ink;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("serif",Typeface.NORMAL);p.textSize=minOf(17f*resources.displayMetrics.widthPixels/393f,height*.36f);val room=if(icon==null)width.toFloat()else width*.66f;if(p.measureText(label)>room-4)p.textSize*=((room-4)/p.measureText(label));c.drawText(label,if(icon==null)width/2f else width*.64f,height/2f-(p.ascent()+p.descent())/2,p)}
 }
}
object GameIcons{
 fun draw(c:Canvas,id:String,color:Int=Theme.ink){val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color;strokeWidth=8f;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND;style=Paint.Style.STROKE}
  when(id){
   "back"->{c.drawLine(66f,20f,34f,50f,p);c.drawLine(34f,50f,66f,80f,p)}
   "undo","new"->{c.drawArc(RectF(22f,22f,82f,82f),-120f,280f,false,p);p.style=Paint.Style.FILL;val a=Path();a.moveTo(20f,11f);a.lineTo(20f,42f);a.lineTo(49f,33f);a.close();c.drawPath(a,p)}
   "gear"->{repeat(8){i->val a=i*PI/4;c.drawLine(50+cos(a).toFloat()*23,50+sin(a).toFloat()*23,50+cos(a).toFloat()*36,50+sin(a).toFloat()*36,p)};c.drawCircle(50f,50f,24f,p);c.drawCircle(50f,50f,7f,p)}
   "zoom","plus","minus"->{if(id=="zoom"){c.drawCircle(42f,42f,25f,p);c.drawLine(62f,62f,83f,83f,p)}else{c.drawLine(23f,50f,77f,50f,p);if(id=="plus")c.drawLine(50f,23f,50f,77f,p)}}
   "notes"->{p.style=Paint.Style.FILL;val a=Path();a.moveTo(21f,73f);a.lineTo(67f,18f);a.lineTo(83f,31f);a.lineTo(37f,84f);a.lineTo(16f,90f);a.close();c.drawPath(a,p)}
   "erase"->{val a=Path();a.moveTo(17f,62f);a.lineTo(56f,20f);a.lineTo(85f,47f);a.lineTo(45f,87f);a.lineTo(35f,87f);a.close();c.drawPath(a,p);c.drawLine(35f,43f,64f,70f,p)}
   "hint"->{c.drawOval(28f,13f,74f,65f,p);c.drawLine(40f,69f,64f,69f,p);c.drawLine(43f,81f,62f,81f,p);c.drawLine(48f,91f,57f,91f,p)}
   "flag"->{c.drawLine(31f,18f,31f,84f,p);p.style=Paint.Style.FILL;val a=Path();a.moveTo(34f,19f);a.lineTo(81f,34f);a.lineTo(34f,50f);a.close();c.drawPath(a,p)}
   "reveal"->{val a=Path();a.moveTo(10f,50f);a.quadTo(50f,8f,90f,50f);a.quadTo(50f,92f,10f,50f);c.drawPath(a,p);c.drawCircle(50f,50f,12f,p)}
   "cross"->{c.drawLine(25f,25f,75f,75f,p);c.drawLine(75f,25f,25f,75f,p)}
   "fill"->{p.style=Paint.Style.FILL;c.drawRoundRect(25f,25f,75f,75f,5f,5f,p)}
  }
 }
}
object PhysicalPieces{
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 fun sprite(c:Context,canvas:Canvas,name:String,r:RectF){ArtAssets.bitmap(c,"piece_${name}_v05")?.let{p.color=Color.WHITE;p.shader=null;canvas.drawBitmap(it,null,r,p)}}
 fun ceramic(c:Context,canvas:Canvas,r:RectF,style:String)=sprite(c,canvas,style,r)
 fun mosaic(c:Context,canvas:Canvas,r:RectF,style:String){ArtAssets.bitmap(c,"piece_${style}_v05")?.let{val mx=(it.width*.11f).toInt();val my=(it.height*.12f).toInt();p.color=Color.WHITE;p.shader=null;canvas.drawBitmap(it,Rect(mx,my,it.width-mx,it.height-my),r,p)};p.color=0x33543014;p.style=Paint.Style.STROKE;p.strokeWidth=.7f;canvas.drawRect(r,p);p.style=Paint.Style.FILL}
 fun socket(c:Canvas,x:Float,y:Float,r:Float,selected:Boolean=false){p.shader=RadialGradient(x,y-r*.3f,r*1.4f,intArrayOf(0xFF1C1007.toInt(),0xFF64401D.toInt()),null,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=maxOf(1f,r*.045f);p.color=if(selected)0xFF58DAD3.toInt()else 0xFFC49350.toInt();c.drawCircle(x,y,r,p);p.style=Paint.Style.FILL}
 fun peg(c:Canvas,x:Float,y:Float,r:Float,color:Int){p.shader=null;p.color=0x99211304.toInt();c.drawCircle(x+1,y+3,r+1,p);p.shader=RadialGradient(x-r*.35f,y-r*.4f,r*1.6f,intArrayOf(ArcadeArt.lighten(color,.42f),color,ArcadeArt.darken(color,.7f)),floatArrayOf(0f,.42f,1f),Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=maxOf(.6f,r*.04f);p.color=0xFFEACA89.toInt();c.drawCircle(x,y,r,p);p.style=Paint.Style.FILL;p.color=0xAAFFFFFF.toInt();c.drawOval(x-r*.52f,y-r*.66f,x-r*.05f,y-r*.37f,p)}
}
class CardBackDrawable(private val c:Context):Drawable(){override fun draw(canvas:Canvas){PhysicalPieces.sprite(c,canvas,"card",RectF(bounds))};override fun setAlpha(a:Int){};override fun setColorFilter(f:ColorFilter?){};override fun getOpacity()=PixelFormat.TRANSLUCENT}
