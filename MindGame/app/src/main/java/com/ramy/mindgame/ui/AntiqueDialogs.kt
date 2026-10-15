package com.ramy.mindgame.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ramy.mindgame.R

/** Shared native popup skin; actions, list choices and validation remain platform controls. */
object AntiqueDialogs {
 class Builder(c:Context):AlertDialog.Builder(c,R.style.AntiqueDialogTheme) {
  override fun show():AlertDialog=super.show().also { decorate(it) }
  override fun create():AlertDialog = super.create().also { dialog ->
   dialog.window?.setBackgroundDrawable(AntiquePanelDrawable(context))
   dialog.setOnShowListener { decorate(dialog) }
   dialog.window?.decorView?.addOnAttachStateChangeListener(object:View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v:View){style(v);dialog.window?.setDimAmount(.65f)}
    override fun onViewDetachedFromWindow(v:View){}
   })
  }
 }
 @JvmStatic fun decorate(dialog:AlertDialog){style(dialog.window!!.decorView)}
 private fun style(v:View){
  when(v){
   is Button->{v.typeface=GameFonts.face(v.context);v.isAllCaps=false;v.setTextColor(Theme.ink);v.backgroundTintList=null;v.background=Theme.button(v.context,""){}.background;v.minHeight=Theme.dp(v.context,44);v.setPadding(Theme.dp(v.context,12),0,Theme.dp(v.context,12),0);(v.layoutParams as? ViewGroup.MarginLayoutParams)?.let{val gap=Theme.dp(v.context,4);it.setMargins(gap,gap,gap,gap);v.layoutParams=it}}
   is EditText->{v.typeface=GameFonts.face(v.context);v.setTextColor(Theme.darkInk);v.backgroundTintList=ColorStateList.valueOf(0xFF8B6633.toInt())}
   is TextView->{v.typeface=GameFonts.face(v.context,v.typeface?.isBold==true||v.id==android.R.id.title||v.id==v.resources.getIdentifier("alertTitle","id","android"));v.setTextColor(Theme.darkInk)}
  }
  if(v is ListView){v.setBackgroundColor(Color.TRANSPARENT);v.divider=android.graphics.drawable.ColorDrawable(0x44724E25);v.dividerHeight=Theme.dp(v.context,1)}
  if(v is ViewGroup)for(i in 0 until v.childCount)style(v.getChildAt(i))
 }
}

/** Wood rim, engraved brass and mottled parchment, rendered locally at any dialog size. */
class AntiquePanelDrawable(private val c:Context):Drawable(){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val d=c.resources.displayMetrics.density
 override fun getPadding(out:Rect):Boolean{val n=(12*d).toInt();out.set(n,n,n,n);return true}
 override fun draw(canvas:Canvas){
  val r=RectF(bounds);val radius=18*d
  p.shader=null;p.style=Paint.Style.FILL;p.color=Theme.bg;canvas.drawRoundRect(r,radius,radius,p)
  canvas.save();val clip=Path();clip.addRoundRect(r,radius,radius,Path.Direction.CW);canvas.clipPath(clip)
  ArtAssets.bitmap(c,"wood")?.let{p.shader=BitmapShader(it,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);canvas.drawRect(r,p);p.shader=null}
  val paper=RectF(r).apply{inset(7*d,7*d)}
  p.shader=LinearGradient(paper.left,paper.top,paper.right,paper.bottom,intArrayOf(0xFFF9ECCC.toInt(),0xFFEBD1A0.toInt(),0xFFF5E4BE.toInt()),null,Shader.TileMode.CLAMP)
  canvas.drawRoundRect(paper,12*d,12*d,p);p.shader=null
  canvas.save();val path=Path();path.addRoundRect(paper,12*d,12*d,Path.Direction.CW);canvas.clipPath(path)
  val w=paper.width().toInt().coerceAtLeast(1);val h=paper.height().toInt().coerceAtLeast(1)
  p.color=0x137A572E;for(i in 0..1800){val x=paper.left+(i*71L%w).toFloat();val y=paper.top+(i*137L%h).toFloat();canvas.drawCircle(x,y,if(i%4==0).9f*d else .4f*d,p)}
  p.shader=RadialGradient(paper.centerX(),paper.centerY(),maxOf(paper.width(),paper.height())*.67f,intArrayOf(0x00A26A2E,0x30A26A2E),null,Shader.TileMode.CLAMP);canvas.drawRect(paper,p);p.shader=null;canvas.restore()
  p.style=Paint.Style.STROKE;p.strokeWidth=1.5f*d;p.color=Theme.brass;canvas.drawRoundRect(RectF(r).apply{inset(2*d,2*d)},radius-2*d,radius-2*d,p);canvas.drawRoundRect(paper,12*d,12*d,p)
  p.strokeWidth=.65f*d;p.color=0xFF8B6633.toInt();canvas.drawRoundRect(RectF(paper).apply{inset(3*d,3*d)},10*d,10*d,p);p.style=Paint.Style.FILL
  for(x in listOf(paper.left+9*d,paper.right-9*d))for(y in listOf(paper.top+9*d,paper.bottom-9*d)){p.color=Theme.brass;canvas.drawCircle(x,y,2*d,p);p.color=0xFFFCEAC4.toInt();canvas.drawCircle(x-.5f*d,y-.5f*d,.65f*d,p)}
  canvas.restore()
 }
 override fun setAlpha(a:Int){};override fun setColorFilter(f:ColorFilter?){};override fun getOpacity()=PixelFormat.TRANSLUCENT
}
