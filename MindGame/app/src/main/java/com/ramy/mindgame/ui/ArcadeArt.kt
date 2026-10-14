package com.ramy.mindgame.ui

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.*

/** Resolution-independent artwork. Real game pieces, no emoji/font dependence. */
object ArcadeArt {
    val p=Paint(Paint.ANTI_ALIAS_FLAG)
    fun color(s: String)=Color.parseColor(s)
    fun oval(c: Canvas,x: Float,y: Float,w: Float,h: Float,color: Int) { p.color=color; p.style=Paint.Style.FILL; p.shader=null; c.drawOval(x,y,x+w,y+h,p) }
    fun circle(c: Canvas,x: Float,y: Float,r: Float,color: Int) { oval(c,x-r,y-r,r*2,r*2,color) }
    fun line(c: Canvas,x: Float,y: Float,x2: Float,y2: Float,color: Int,width: Float=3f) { p.shader=null;p.color=color;p.strokeWidth=width;p.strokeCap=Paint.Cap.ROUND;c.drawLine(x,y,x2,y2,p) }
    fun round(c: Canvas,x: Float,y: Float,w: Float,h: Float,r: Float,color: Int) { p.shader=null;p.style=Paint.Style.FILL;p.color=color;c.drawRoundRect(x,y,x+w,y+h,r,r,p) }
    fun path(c: Canvas,color: Int,vararg points: Float) { val path=Path(); path.moveTo(points[0],points[1]); for(i in 2 until points.size step 2) path.lineTo(points[i],points[i+1]); path.close(); p.shader=null;p.color=color;p.style=Paint.Style.FILL;c.drawPath(path,p) }
    fun text(c: Canvas,s: String,x: Float,y: Float,size: Float,color: Int) { p.shader=null;p.style=Paint.Style.FILL;p.color=color;p.textSize=size;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD);c.drawText(s,x,y,p) }
    fun leaf(c: Canvas,x: Float,y: Float,angle: Float=0f) { c.save(); c.rotate(angle,x,y); oval(c,x,y-10,22f,10f,color("#55B97B")); line(c,x,y-4,x+17,y-5,color("#258B54"),1.4f);c.restore() }
    fun fruit(c: Canvas,index: Int) {
        // 100×100 illustration space.
        val cream=color("#FFF4DC"); val red=color("#F75B6C"); val dark=color("#A92D49")
        when(index%8) {
            0 -> { line(c,36f,54f,52f,19f,color("#4E7C50"),4f);line(c,67f,57f,52f,19f,color("#4E7C50"),4f);leaf(c,53f,21f,-18f);circle(c,33f,64f,20f,dark);circle(c,33f,61f,19f,red);circle(c,66f,68f,20f,dark);circle(c,66f,65f,19f,red);oval(c,24f,47f,8f,13f,color("#FFB9B5"));oval(c,58f,52f,8f,13f,color("#FFB9B5")) }
            1 -> { val body=Path();body.moveTo(20f,35f);body.cubicTo(18f,62f,34f,87f,50f,93f);body.cubicTo(67f,84f,83f,56f,80f,35f);body.cubicTo(70f,22f,29f,22f,20f,35f);p.color=red;p.shader=null;c.drawPath(body,p);path(c,color("#49A96B"),20f,30f,39f,30f,50f,12f,57f,29f,81f,27f,66f,42f,52f,34f,37f,42f);for(r in 0..2) for(k in 0..2) oval(c,32f+k*15+(r%2)*3,46f+r*13,3f,5f,cream) }
            2 -> { c.save();c.rotate(-22f,50f,55f);oval(c,16f,32f,70f,48f,color("#E0A836"));oval(c,13f,27f,70f,48f,color("#FFDF64"));oval(c,24f,36f,40f,10f,color("#FFF2AB"));c.restore();leaf(c,68f,29f,-25f) }
            3 -> { val path=Path();path.moveTo(13f,30f);path.lineTo(87f,30f);path.arcTo(RectF(13f,-7f,87f,67f),0f,180f,false);path.close();p.color=color("#53B17A");c.drawPath(path,p);val inner=Path();inner.moveTo(18f,30f);inner.lineTo(82f,30f);inner.arcTo(RectF(18f,-2f,82f,62f),0f,180f,false);inner.close();p.color=color("#FFEBD0");c.drawPath(inner,p);val flesh=Path();flesh.moveTo(23f,30f);flesh.lineTo(77f,30f);flesh.arcTo(RectF(23f,3f,77f,57f),0f,180f,false);flesh.close();p.color=red;c.drawPath(flesh,p);repeat(5) { k -> oval(c,30f+k*8,36f+(if(k%2==0) 3 else 9),3f,5f,dark) }; c.translate(0f,9f) }
            4 -> { circle(c,50f,54f,36f,color("#8D644C"));circle(c,50f,52f,32f,color("#AADD75"));circle(c,50f,52f,11f,cream);repeat(12) { i -> val a=i*PI/6; oval(c,50f+cos(a).toFloat()*20-1.5f,52f+sin(a).toFloat()*20-2,3f,4f,color("#3D5C40")) };circle(c,43f,43f,3f,Color.WHITE) }
            5 -> { circle(c,50f,56f,33f,color("#D57A2E"));circle(c,50f,52f,32f,color("#FFAC4E"));oval(c,30f,30f,12f,20f,color("#FFD19A"));leaf(c,51f,24f,-18f);line(c,50f,27f,51f,15f,color("#49754A"),4f) }
            6 -> { for(i in 0..4) { val x=listOf(31f,64f,48f,27f,68f)[i];val y=listOf(47f,43f,63f,70f,68f)[i];circle(c,x,y,17f,color("#3B477E"));circle(c,x,y-3,16f,color("#6E83C4"));circle(c,x-5,y-9,4f,color("#ADBAE3"));path(c,color("#424F83"),x-5,y-3,x,y-7,x+5,y-3,x+3,y+2,x-3,y+2) };leaf(c,47f,24f,-12f) }
            else -> { oval(c,17f,30f,40f,58f,dark);oval(c,45f,30f,40f,58f,dark);oval(c,17f,27f,40f,56f,red);oval(c,45f,27f,40f,56f,red);round(c,27f,48f,48f,33f,10f,red);line(c,51f,33f,54f,15f,color("#81674D"),5f);leaf(c,53f,24f,-20f);oval(c,27f,37f,9f,20f,color("#FFB8B2")) }
        }
    }
    fun peg(c: Canvas,x: Float,y: Float,r: Float,col: Int) {
        circle(c,x,y+4,r+1,color("#0C101C"));p.shader=RadialGradient(x-r*.3f,y-r*.4f,r*1.8f,intArrayOf(lighten(col,.48f),col,darken(col,.5f)),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null;oval(c,x-r*.53f,y-r*.65f,r*.7f,r*.32f,0x66FFFFFF);circle(c,x+r*.25f,y+r*.5f,r*.10f,darken(col,.35f))
    }
    fun flag(c: Canvas) { line(c,42f,25f,42f,77f,color("#FFE9C8"),6f);path(c,color("#CDA044"),44f,22f,78f,32f,44f,49f);round(c,25f,75f,42f,8f,4f,color("#23384B")) }
    fun mine(c: Canvas) { repeat(8) { i -> val a=i*PI/4;line(c,50f+cos(a).toFloat()*15,50f+sin(a).toFloat()*15,50f+cos(a).toFloat()*36,50f+sin(a).toFloat()*36,color("#2C304B"),6f) };circle(c,50f,50f,25f,color("#303650"));circle(c,50f,47f,22f,color("#454D71"));circle(c,42f,39f,7f,color("#AEB9D4")) }
    fun lighten(col: Int,f: Float)=Color.rgb((Color.red(col)+(255-Color.red(col))*f).toInt(),(Color.green(col)+(255-Color.green(col))*f).toInt(),(Color.blue(col)+(255-Color.blue(col))*f).toInt())
    fun darken(col: Int,f: Float)=Color.rgb((Color.red(col)*(1-f)).toInt(),(Color.green(col)*(1-f)).toInt(),(Color.blue(col)*(1-f)).toInt())
}

class PieceDrawable(private val type: String,private val value: Int=0,private val raised: Boolean=true,private val matched: Boolean=false): Drawable() {
    override fun draw(canvas: Canvas) {
        val c=canvas;c.save();c.translate(bounds.left.toFloat(),bounds.top.toFloat());if(type=="peg") { val side=minOf(bounds.width(),bounds.height()).toFloat();c.translate((bounds.width()-side)/2,(bounds.height()-side)/2);c.scale(side/100f,side/100f) } else c.scale(bounds.width()/100f,bounds.height()/100f)
        val a=ArcadeArt
        when(type) {
            "memory-back" -> { a.round(c,3f,8f,94f,89f,14f,a.color("#403019"));a.round(c,3f,3f,94f,88f,14f,a.color("#1D5853"));a.round(c,9f,9f,82f,76f,10f,a.color("#246F66"));for(r in 0..4) for(k in 0..4) { val x=16f+k*17;val y=16f+r*16;a.path(c,a.color("#3C8577"),x,y-4,x+4,y,x,y+4,x-4,y) };a.circle(c,50f,47f,23f,a.color("#143F3A"));a.path(c,a.color("#FFD392"),50f,26f,55f,39f,69f,44f,57f,51f,53f,66f,45f,54f,31f,49f,44f,42f) }
            "fruit" -> { a.round(c,3f,8f,94f,89f,14f,a.color("#BCB39F"));a.round(c,3f,3f,94f,88f,14f,a.color(if(matched) "#DDF4C7" else "#FFF5DF"));c.save();c.translate(5f,0f);c.scale(.9f,.9f);a.fruit(c,value);c.restore();if(matched) { a.circle(c,83f,16f,10f,a.color("#53A778"));a.line(c,78f,16f,82f,20f,Color.WHITE,2f);a.line(c,82f,20f,88f,12f,Color.WHITE,2f) } }
            "peg" -> {a.peg(c,50f,47f,36f,value);if(matched){a.p.shader=null;a.p.style=Paint.Style.STROKE;a.p.strokeWidth=3f;a.p.color=Theme.brass;c.drawCircle(50f,47f,44f,a.p);a.p.style=Paint.Style.FILL}}
            "mines" -> {
                if(raised) { a.round(c,2f,8f,96f,91f,9f,a.color("#AA895D"));a.round(c,2f,2f,96f,89f,9f,a.color("#D2BA8E"));a.round(c,7f,7f,86f,77f,6f,a.color("#E2CBA4"));a.line(c,12f,12f,84f,12f,a.color("#F6E7C8"),3f) } else a.round(c,2f,2f,96f,96f,6f,a.color("#F0DEB9"))
                if(value==-1) a.flag(c) else if(value==-2) a.mine(c) else if(value>0) a.text(c,"$value",50f,70f,58f,listOf(a.color("#337AAD"),a.color("#3E8B6A"),a.color("#CB5968"),a.color("#7758A1"))[(value-1)%4])
            }
            "tile" -> {
                val colors=listOf("#F2DEB8","#3B9290","#C8713F","#BC5036","#D8A341","#2D7774","#C89D4A","#A9583B","#22605F","#9C7132","#775030")
                val index=if(value==0) 0 else ((ln(value.toDouble())/ln(2.0)).toInt()-1).coerceIn(0,10)
                val col=if(value==0) a.color("#AA906A") else a.color(colors[index])
                a.round(c,2f,8f,96f,90f,12f,a.darken(col,.32f));a.round(c,2f,2f,96f,87f,12f,col)
                if(value>0) { a.line(c,8f,20f,17f,26f,0x22FFFFFF,1f);a.line(c,17f,26f,14f,40f,0x22FFFFFF,1f);a.line(c,80f,68f,87f,82f,0x2250300A,1f);a.line(c,15f,11f,83f,11f,a.lighten(col,.4f),3f);a.text(c,"$value",50f,61f,if(value>=1024) 30f else if(value>=128) 36f else 45f,if(index==0) a.color("#39271A") else a.color("#FFF1D4")) }
            }
            "pixel" -> { val col=if(value==1) a.color("#9BD294") else a.color("#E0E5D8");a.round(c,3f,7f,94f,91f,9f,a.darken(col,.22f));a.round(c,3f,3f,94f,89f,9f,col);if(value==1) a.line(c,16f,13f,84f,13f,a.lighten(col,.3f),3f);if(value==2) { a.line(c,39f,39f,61f,61f,a.color("#96A297"),4f);a.line(c,61f,39f,39f,61f,a.color("#96A297"),4f) } }
        }
        c.restore()
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity()=PixelFormat.TRANSLUCENT
}

class ArcadeBackdrop(private val accent: Int=ArcadeArt.color("#577096")): Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        p.shader=LinearGradient(0f,0f,bounds.width().toFloat(),bounds.height().toFloat(),intArrayOf(ArcadeArt.color("#15263A"),ArcadeArt.color("#111B2D")),null,Shader.TileMode.CLAMP);c.drawRect(bounds,p);p.shader=null
        p.color=accent;p.alpha=18;c.drawCircle(bounds.width()*.9f,bounds.height()*.14f,bounds.width()*.6f,p);p.alpha=12;c.drawCircle(bounds.width()*.05f,bounds.height()*.65f,bounds.width()*.6f,p);p.alpha=255
        p.color=0x143F7694;for(x in 0..bounds.width() step 40) for(y in 0..bounds.height() step 40) c.drawCircle(x.toFloat(),y.toFloat(),1.4f,p)
    }
    override fun setAlpha(alpha: Int) {} ; override fun setColorFilter(cf: ColorFilter?) {} ; override fun getOpacity()=PixelFormat.OPAQUE
}

class SudokuCellDrawable(private val index: Int,private val selected: Boolean,private val related: Boolean,private val given: Boolean): Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        val a=ArcadeArt;val b=bounds;val r=index/9;val col=index%9
        p.color=a.color(when { selected->"#74B8AB";related->"#D9DFC4";given->"#FCF3DE";else->"#FFF9ED" });c.drawRect(b,p)
        p.color=a.color("#D6C9AA");p.strokeWidth=1f;c.drawLine(b.left.toFloat(),b.top.toFloat(),b.right.toFloat(),b.top.toFloat(),p);c.drawLine(b.left.toFloat(),b.top.toFloat(),b.left.toFloat(),b.bottom.toFloat(),p)
        p.color=a.color("#8D806B");p.strokeWidth=maxOf(3f,b.width()*.055f)
        if(r%3==0)c.drawLine(b.left.toFloat(),b.top.toFloat(),b.right.toFloat(),b.top.toFloat(),p)
        if(col%3==0)c.drawLine(b.left.toFloat(),b.top.toFloat(),b.left.toFloat(),b.bottom.toFloat(),p)
        if(r==8)c.drawLine(b.left.toFloat(),b.bottom.toFloat()-1,b.right.toFloat(),b.bottom.toFloat()-1,p)
        if(col==8)c.drawLine(b.right.toFloat()-1,b.top.toFloat(),b.right.toFloat()-1,b.bottom.toFloat(),p)
    }
    override fun setAlpha(alpha: Int) {};override fun setColorFilter(cf: ColorFilter?) {};override fun getOpacity()=PixelFormat.OPAQUE
}
