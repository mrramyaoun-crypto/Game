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
    override fun draw(c: Canvas) { val r=RectF(bounds);p.color=0xFFBD9F63.toInt();c.drawRoundRect(r,8f,8f,p);val inside=RectF(r.left+2,r.top+2,r.right-2,r.bottom-4);c.save();val path=Path();path.addRoundRect(inside,7f,7f,Path.Direction.CW);c.clipPath(path);ArtAssets.bitmap(context,"relic_$relic")?.let{c.drawBitmap(it,null,inside,p)};c.restore();if(matched) {p.color=0xFF1E7063.toInt();c.drawCircle(r.right-12,r.top+12,9f,p);p.color=Color.WHITE;p.strokeWidth=2f;c.drawLine(r.right-16,r.top+12,r.right-13,r.top+15,p);c.drawLine(r.right-13,r.top+15,r.right-8,r.top+9,p)} }
    override fun setAlpha(a: Int) {};override fun setColorFilter(f: ColorFilter?) {};override fun getOpacity()=PixelFormat.OPAQUE
}
