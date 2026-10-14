package com.ramy.mindgame.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import kotlin.math.*

/** Home scene artwork with independent native controls over the actual physical objects. */
data class DeskResume(val id: String,val name: String,val detail: String,val time: String,val progress: Float?,val hasSave: Boolean,val button: String)

class ExplorerDesk(
    context: Context,
    private val motion: () -> Boolean,
    private val resume: DeskResume,
    private val open: (String) -> Unit,
    private val settings: () -> Unit,
    private val library: () -> Unit,
    private val collection: () -> Unit,
    private val stats: () -> Unit,
    private val feedback: (View) -> Unit
): FrameLayout(context) {
    companion object {const val ART_WIDTH=887f; const val ART_HEIGHT=1774f}
    private val art=ArtAssets.bitmap(context,"explorer_scene_v04") ?: error("Explorer desk artwork is missing")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val targets=linkedMapOf<String,DeskTarget>()
    private var artScale=1f
    private var artLeft=0f
    private var artTop=0f
    private var active: DeskTarget?=null
    private var glow=0f
    private var animator: ValueAnimator?=null
    private val ink=Color.rgb(50,33,20)
    private val brass=Color.rgb(216,174,91)
    private val ivory=Color.rgb(255,242,210)
    private val regular=Typeface.create("serif",Typeface.NORMAL)
    private val bold=Typeface.create("serif",Typeface.BOLD)

    private inner class DeskTarget(val key: String,val artBounds: RectF,label: String,val action: ()->Unit): View(context) {
        init {tag="desk:$key";contentDescription=label;isClickable=true;isFocusable=true;background=null
            setOnClickListener {feedback(it);action()}
            setOnFocusChangeListener {_,hasFocus->if(hasFocus){active=this;glow=.3f}else if(active===this){active=null;glow=0f};this@ExplorerDesk.invalidate()}
        }
        override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {super.onInitializeAccessibilityNodeInfo(info);info.className="android.widget.Button"}
        override fun onTouchEvent(e: MotionEvent): Boolean {
            when(e.actionMasked){
                MotionEvent.ACTION_DOWN->{active=this;animateGlow(if(motion())1f else .45f);isPressed=true}
                MotionEvent.ACTION_MOVE->{if(e.x<0||e.y<0||e.x>width||e.y>height){isPressed=false;animateGlow(0f)}}
                MotionEvent.ACTION_UP->{val click=isPressed&&e.x in 0f..width.toFloat()&&e.y in 0f..height.toFloat();isPressed=false;animateGlow(0f);if(click)performClick()}
                MotionEvent.ACTION_CANCEL->{isPressed=false;animateGlow(0f)}
            };return true
        }
    }
    init {
        setWillNotDraw(false);clipChildren=false;importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        fun target(id: String,b: RectF,label: String,action: ()->Unit){val v=DeskTarget(id,b,label,action);targets[id]=v;addView(v,LayoutParams(0,0))}
        target("enigma",RectF(8f,268f,405f,587f),"Enigma. Tap the cipher machine to play or resume."){open("enigma")}
        target("mastermind",RectF(413f,295f,838f,587f),"Mastermind. Tap the wooden peg board to play or resume."){open("mastermind")}
        target("memory",RectF(4f,594f,413f,963f),"Relic Hunt. Tap the antique cards to play or resume."){open("memory")}
        target("nonogram",RectF(419f,590f,879f,966f),"Mosaic Restoration. Tap the owl mosaic to play or resume."){open("nonogram")}
        target("sudoku",RectF(7f,974f,308f,1312f),"Sudoku. Tap the paper puzzle to play or resume."){open("sudoku")}
        target("mines",RectF(312f,974f,602f,1312f),"Minesweeper. Tap the flagged board to play or resume."){open("mines")}
        target("2048",RectF(608f,974f,882f,1312f),"2048. Tap the ceramic tiles to play or resume."){open("2048")}
        target("settings",RectF(744f,5f,871f,130f),"Settings"){settings()}
        target("resume",RectF(25f,1425f,865f,1634f),"${resume.button} ${resume.name}. ${resume.detail}"){open(resume.id)}
        target("library",RectF(24f,1638f,300f,1767f),"Library. Explorer’s Desk."){library()}
        target("collection",RectF(304f,1638f,585f,1767f),"Restoration collection"){collection()}
        target("stats",RectF(590f,1638f,866f,1767f),"Your statistics"){stats()}
    }
    private fun animateGlow(to: Float){animator?.cancel();if(!motion()){glow=to;invalidate();return};animator=ValueAnimator.ofFloat(glow,to).apply{duration=110;addUpdateListener{glow=it.animatedValue as Float;this@ExplorerDesk.invalidate()};start()}}
    override fun onSizeChanged(w: Int,h: Int,oldw: Int,oldh: Int){artScale=minOf(w/ART_WIDTH,h/ART_HEIGHT);artLeft=(w-ART_WIDTH*artScale)/2;artTop=(h-ART_HEIGHT*artScale)/2}
    override fun onLayout(changed: Boolean,l: Int,t: Int,r: Int,b: Int){
        targets.values.forEach {v->val a=v.artBounds;val x=artLeft+a.left*artScale;val y=artTop+a.top*artScale;val ww=a.width()*artScale;val hh=a.height()*artScale;val minimum=48*resources.displayMetrics.density;val extraX=maxOf(0f,(minimum-ww)/2);val extraY=maxOf(0f,(minimum-hh)/2);v.layout((x-extraX).roundToInt().coerceAtLeast(0),(y-extraY).roundToInt().coerceAtLeast(0),(x+ww+extraX).roundToInt().coerceAtMost(width),(y+hh+extraY).roundToInt().coerceAtMost(height))}
    }
    /** Coordinates used by gesture tests are taken from the real native target layout. */
    fun targetBounds(key: String): Rect {val v=targets.getValue(key);return Rect(v.left,v.top,v.right,v.bottom)}
    private fun text(c: Canvas,value: String,x: Float,y: Float,size: Float,color: Int=ink,weight: Boolean=true,maxWidth: Float=Float.MAX_VALUE,angle: Float=0f){
        paint.shader=null;paint.style=Paint.Style.FILL;paint.typeface=if(weight)bold else regular;paint.textAlign=Paint.Align.CENTER;paint.textSize=size;paint.color=color
        if(paint.measureText(value)>maxWidth)paint.textSize*=maxWidth/paint.measureText(value)
        c.save();if(angle!=0f)c.rotate(angle,x,y);c.drawText(value,x,y,paint);c.restore()
    }
    private fun gear(c: Canvas){c.save();c.translate(806f,74f);paint.shader=null;paint.color=brass;paint.style=Paint.Style.FILL
        repeat(8){c.save();c.rotate(it*45f);c.drawRoundRect(-4f,-23f,4f,-13f,1.5f,1.5f,paint);c.restore()};c.drawCircle(0f,0f,16f,paint);paint.color=Color.rgb(50,31,16);c.drawCircle(0f,0f,9f,paint);c.restore()}
    private fun navIcon(c: Canvas,key: String,x: Float,y: Float){
        paint.shader=null;paint.color=if(key=="library")Color.rgb(120,222,208)else brass;paint.style=Paint.Style.FILL;c.save();c.translate(x,y)
        when(key){
            "library"->{val p=Path();p.moveTo(-21f,0f);p.lineTo(0f,-21f);p.lineTo(21f,0f);p.lineTo(15f,0f);p.lineTo(15f,21f);p.lineTo(4f,21f);p.lineTo(4f,6f);p.lineTo(-4f,6f);p.lineTo(-4f,21f);p.lineTo(-15f,21f);p.lineTo(-15f,0f);p.close();c.drawPath(p,paint)}
            "collection"->{paint.style=Paint.Style.STROKE;paint.strokeWidth=4f;c.drawRoundRect(-23f,-5f,23f,20f,3f,3f,paint);c.drawRoundRect(-23f,-16f,23f,2f,4f,4f,paint);paint.style=Paint.Style.FILL;c.drawRect(-4f,-1f,4f,9f,paint)}
            "stats"->{c.drawRect(-22f,0f,-12f,21f,paint);c.drawRect(-5f,-20f,5f,21f,paint);c.drawRect(12f,-8f,22f,21f,paint)}
        };paint.style=Paint.Style.FILL;c.restore()
    }
    override fun onDraw(c: Canvas){
        super.onDraw(c);c.drawColor(Color.rgb(35,22,14));c.save();c.translate(artLeft,artTop);c.scale(artScale,artScale);paint.shader=null;paint.color=Color.WHITE;c.drawBitmap(art,null,RectF(0f,0f,ART_WIDTH,ART_HEIGHT),paint)
        text(c,"MindGame",490f,93f,74f,ivory,false,460f)
        text(c,"Explorer’s Desk",445f,177f,43f,ivory,false,470f)
        gear(c)
        text(c,"Enigma",247f,548f,34f,maxWidth=252f,angle=-4f)
        text(c,"Mastermind",641f,551f,34f,maxWidth=222f)
        text(c,"Relic Hunt",195f,913f,34f,maxWidth=245f)
        text(c,"Mosaic Restoration",636f,939f,32f,maxWidth=265f)
        text(c,"Sudoku",155f,1282f,33f,maxWidth=222f)
        text(c,"Minesweeper",456f,1289f,32f,maxWidth=220f)
        text(c,"2048",739f,1280f,34f,maxWidth=202f)
        val resumeTitle=if(resume.hasSave)"Resume your puzzle" else "Choose your first puzzle"
        paint.typeface=bold;paint.color=ink;paint.textSize=33f;paint.textAlign=Paint.Align.LEFT;c.drawText(resumeTitle,54f,1474f,paint)
        val thumb=if(resume.id=="nonogram")ArtAssets.bitmap(context,"explorer_owl_resume_v04")else ArtAssets.bitmap(context,ArtAssets.gameArt[resume.id]?:"explorer_owl_resume_v04")
        thumb?.let{paint.color=Color.WHITE;c.drawBitmap(it,null,RectF(101f,1510f,208f,1603f),paint)}
        paint.color=ink;paint.typeface=regular;paint.textAlign=Paint.Align.LEFT;paint.textSize=29f
        var nameSize=29f;val textWidth=375f;if(paint.measureText(resume.name)>textWidth)nameSize*=textWidth/paint.measureText(resume.name);paint.textSize=nameSize;c.drawText(resume.name,238f,1534f,paint)
        paint.textSize=26f;c.drawText(resume.detail,238f,1568f,paint)
        if(resume.progress!=null){paint.color=Color.rgb(91,73,42);c.drawRoundRect(238f,1586f,482f,1601f,7f,7f,paint);paint.shader=LinearGradient(238f,1586f,482f,1601f,intArrayOf(Color.rgb(149,200,161),Color.rgb(67,133,111)),null,Shader.TileMode.CLAMP);c.drawRoundRect(238f,1586f,238f+244f*resume.progress.coerceIn(0f,1f),1601f,7f,7f,paint);paint.shader=null}
        text(c,resume.time,551f,1602f,23f,ink,false,99f)
        text(c,resume.button,727f,1556f,36f,ivory,false,173f)
        for((key,label,x)in listOf(Triple("library","Library",159f),Triple("collection","Collection",444f),Triple("stats","Stats",726f))){navIcon(c,key,x,1689f);text(c,label,x,1746f,31f,if(key=="library")Color.rgb(135,218,209)else ivory,false,230f)}
        val current=active
        if(current!=null&&glow>0f){val r=current.artBounds;val cx=r.centerX();val cy=r.centerY();val radius=maxOf(r.width(),r.height())*.6f;paint.shader=RadialGradient(cx,cy,radius,intArrayOf(Color.argb((glow*42).toInt(),255,228,170),Color.TRANSPARENT),null,Shader.TileMode.CLAMP);c.drawOval(r,paint);paint.shader=null}
        c.restore()
    }
    override fun onDetachedFromWindow(){animator?.cancel();super.onDetachedFromWindow()}
}
