package com.ramy.mindgame
import android.content.Context
import android.graphics.*
import android.view.View
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
private val pegColors=intArrayOf(0xFFEF2028.toInt(),0xFFF8CB16.toInt(),0xFF10B843.toInt(),0xFF087BE4.toInt(),0xFF9624CD.toInt(),0xFFF49419.toInt())
private val pegNames=listOf("red","gold","green","blue","violet","orange")
internal fun MainActivity.code(g:CodeGame){
 val t=gameTable!!;codeInput=g.draft;var slot=codeInput.indexOf(-1).coerceAtLeast(0)
 t.text({"Mastermind"},70f,8f,253f,37f,26f,Theme.darkInk,true)
 t.text({if(g.won)"Code solved"else if(g.lost)"Code revealed"else "Attempt ${g.moves+1} / 10"},19f,45f,197f,24f,14f,Theme.darkInk,false,Paint.Align.LEFT)
 t.text({elapsedLabel()},287f,45f,86f,24f,14f,Theme.darkInk,false,Paint.Align.RIGHT)
 t.place(SecretPegs(this,g),103f,65f,187f,52f);t.place(CodeBoard(this,g),25f,136f,342f,400f)
 val positions=mutableListOf<PegControl>();var submit:GameControl?=null
 fun refresh(){positions.forEachIndexed{i,v->v.contentDescription="Position ${i+1}, ${if(codeInput[i]<0)"empty"else pegNames[codeInput[i]]}, tap to select";v.invalidate()};submit?.isEnabled=codeInput.all{it>=0}&&!g.won&&!g.lost;persist();t.refresh()}
 repeat(4){i->val v=PegControl(this,{codeInput[i].let{if(it<0)null else pegColors[it]}},{slot==i},false);v.setOnClickListener{slot=i;haptic(v);refresh()};t.place(v,49f+i*74f,546f,72f,61f);positions.add(v)}
 t.text({if(g.won)"Solved! · score ${g.score}"else if(g.lost)"Ten guesses used · start again from the menu"else "Tap a color to fill a slot"},16f,609f,361f,24f,14f,Theme.darkInk)
 listOf(0,3,2,1,4,5).forEachIndexed{k,i->val v=PegControl(this,{pegColors[i]},{false},true);v.contentDescription="Choose ${pegNames[i]}";v.setOnClickListener{if(!g.won&&!g.lost){codeInput[slot]=i;slot=codeInput.indexOf(-1).let{if(it>=0)it else(slot+1)%4};pop(v);haptic(v);refresh()}};t.place(v,13f+k*61f,638f,61f,57f)}
 t.text({"Black = exact · ivory = right color elsewhere"},12f,697f,369f,19f,11f,Theme.darkInk)
 t.control("Clear guess","undo",21f,719f,53f,52f){if(!g.won&&!g.lost){g.draft.fill(-1);slot=0;refresh()}}.ink=Theme.darkInk
 submit=t.control("Check guess",null,91f,719f,214f,52f){if(codeInput.all{it>=0}&&!g.won&&!g.lost){g.submit(codeInput);g.draft=IntArray(4){-1};codeInput=g.draft;changed();playPage()}}
 refresh()
}
private class PegControl(c:Context,private val color:()->Int?,private val selected:()->Boolean,private val palette:Boolean):View(c){
 init{isFocusable=true;isClickable=true}
 override fun getAccessibilityClassName():CharSequence="android.widget.Button"
 override fun onDraw(c:Canvas){val r=minOf(width,height)*if(palette).42f else .35f;val x=width/2f;val y=height/2f;PhysicalPieces.socket(c,x,y,r+2,selected());color()?.let{PhysicalPieces.peg(c,x,y,r,it)}}
 override fun drawableStateChanged(){super.drawableStateChanged();invalidate()}
}
private class SecretPegs(c:Context,private val g:CodeGame):View(c){private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){val step=width/4f;repeat(4){i->val x=step*(i+.5f);val y=height/2f;val r=minOf(step,height.toFloat())*.36f;PhysicalPieces.socket(c,x,y,r+2);if(g.won||g.lost)PhysicalPieces.peg(c,x,y,r,pegColors[g.secret[i]])else{p.color=Theme.brass;p.textSize=r*1.25f;p.typeface=Typeface.create("serif",Typeface.BOLD);p.textAlign=Paint.Align.CENTER;c.drawText("?",x,y-(p.ascent()+p.descent())/2,p)}}}
}
private class CodeBoard(c:Context,private val g:CodeGame):View(c){private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(c:Canvas){val sx=width/342f;val h=height/10f;repeat(10){r->val y=h*(r+.5f)
  if(r==g.attempts.size&&!g.won&&!g.lost){p.color=0x18FFE4AD;c.drawRect(1f,r*h,width-1f,(r+1)*h,p)}
  p.color=0x99764726.toInt();p.strokeWidth=.8f*sx;if(r>0)c.drawLine(0f,r*h,width.toFloat(),r*h,p)
  p.color=Theme.ink;p.textSize=14*sx;p.typeface=Typeface.create("serif",Typeface.NORMAL);p.textAlign=Paint.Align.CENTER;c.drawText("${r+1}",18*sx,y-(p.ascent()+p.descent())/2,p)
  repeat(4){i->val x=(74+i*55)*sx;val radius=minOf(h*.34f,15*sx);PhysicalPieces.socket(c,x,y,radius+1.5f);g.attempts.getOrNull(r)?.let{PhysicalPieces.peg(c,x,y,radius,pegColors[it[i]])}}
  val f=g.attempts.getOrNull(r)?.let{feedback(g.secret,it)}
  repeat(4){i->val x=(302+(i%2)*15)*sx;val yy=y+(i/2-.5f)*15*sx;PhysicalPieces.socket(c,x,yy,4.8f*sx);if(f!=null&&i<f.exact+f.misplaced)PhysicalPieces.peg(c,x,yy,4.2f*sx,if(i<f.exact)0xFF171511.toInt()else 0xFFFFF6DF.toInt())}
 }}
}
