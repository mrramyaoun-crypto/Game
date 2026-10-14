package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.*
import kotlin.math.*

object ArtAssets {
    private val cache=mutableMapOf<String,Bitmap>()
    fun bitmap(c: Context,name: String): Bitmap? = cache[name] ?: try { c.assets.open("art/$name.webp").use { BitmapFactory.decodeStream(it) }?.also {cache[name]=it} } catch(_: Exception) {null}
    val gameArt=mapOf("enigma" to "desk_0","mastermind" to "desk_1","memory" to "desk_2","nonogram" to "desk_3","sudoku" to "desk_4","mines" to "desk_5","2048" to "desk_6")
    fun image(c: Context,id: String)=android.widget.ImageView(c).apply {setImageBitmap(bitmap(c,gameArt[id]?:id));scaleType=android.widget.ImageView.ScaleType.CENTER_CROP;contentDescription=null}
}
class MaterialDrawable(private val context: Context,private val paper: Boolean=false): Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        val r=RectF(bounds);val rad=if(paper) 8f else 0f
        p.shader=null;p.style=Paint.Style.FILL;p.color=if(paper) 0xFFF0DEB9.toInt() else 0xFF3E281B.toInt();c.drawRoundRect(r,rad,rad,p)
        if(!paper) ArtAssets.bitmap(context,"wood")?.let { p.shader=BitmapShader(it,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT); c.drawRect(r,p);p.shader=null;p.color=0x40241408;c.drawRect(r,p) }
        if(paper) {p.color=0x11917343;for(i in 0..120) c.drawCircle(r.left+(i*71%maxOf(1,bounds.width())),r.top+(i*37%maxOf(1,bounds.height())),.7f,p);p.style=Paint.Style.STROKE;p.strokeWidth=2f;p.color=Theme.brass;c.drawRoundRect(r,rad,rad,p);p.style=Paint.Style.FILL}
    }
    override fun setAlpha(a: Int) {} ;override fun setColorFilter(f: ColorFilter?) {};override fun getOpacity()=PixelFormat.OPAQUE
}
class RelicDrawable(private val context: Context,private val relic: Int,private val matched: Boolean): Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    override fun draw(c:Canvas){val r=RectF(bounds);val inset=maxOf(2f,r.width()*.035f);val inside=RectF(r.left+inset,r.top+inset,r.right-inset,r.bottom-inset*2)
        p.color=0x99412A12.toInt();c.drawRoundRect(RectF(r.left+2,r.top+4,r.right,r.bottom),5f,5f,p);p.color=0xFFF2DFB8.toInt();c.drawRoundRect(inside,5f,5f,p)
        c.save();val path=Path();path.addRoundRect(inside,5f,5f,Path.Direction.CW);c.clipPath(path);ArtAssets.bitmap(context,"relic_$relic")?.let{val side=minOf(inside.width(),inside.height());c.drawBitmap(it,null,RectF(inside.centerX()-side/2,inside.centerY()-side/2,inside.centerX()+side/2,inside.centerY()+side/2),p)};c.restore()
        p.color=if(matched)0xFFFFD76B.toInt()else 0xFFB48A45.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=if(matched)2.5f else 1f;c.drawRoundRect(inside,5f,5f,p);p.strokeWidth=.7f;c.drawRoundRect(RectF(inside.left+3,inside.top+3,inside.right-3,inside.bottom-3),4f,4f,p);p.style=Paint.Style.FILL
        if(matched){p.color=0x22FFCF50;c.drawRoundRect(inside,5f,5f,p)}
    }
    override fun setAlpha(a: Int) {};override fun setColorFilter(f: ColorFilter?) {};override fun getOpacity()=PixelFormat.OPAQUE
}
