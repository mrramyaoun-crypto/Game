package com.ramy.mindgame
import android.app.AlertDialog
import android.graphics.Paint
import android.widget.TextView
import com.ramy.mindgame.games.MemoryGame
import com.ramy.mindgame.ui.*
internal fun MainActivity.memory(g:MemoryGame){
 val t=gameTable!!;val rows=g.cards.size/4;val cardH=if(rows<=4)126f else 108f;val gridY=124f;val gridH=rows*cardH;t.designHeight=maxOf(786f,gridY+gridH+120)
 val names=listOf("compass","key","hourglass","telescope","coin","wax seal","quill","map","pocket watch","scarab","sextant","lamp","chest","book","crystal vial","feather")
 t.text({"Memory Challenge"},62f,10f,269f,37f,23f)
 t.text({"—   R E L I C   H U N T   —"},54f,50f,285f,27f,12f,Theme.brass)
 t.text({"${g.matched.sum()/2} / ${g.pairs} pairs"},17f,84f,111f,25f,14f,Theme.ink,false,Paint.Align.LEFT)
 t.place(HuntProgress(this,{g.matched.sum()/2f/g.pairs}),133f,92f,114f,11f)
 t.text({"${g.moves} moves  |  ${elapsedLabel()}"},250f,84f,126f,25f,13f,Theme.ink,false,Paint.Align.RIGHT)
 var animating=false
 fun refresh(){cells.forEachIndexed{i,v->val shown=g.matched[i]==1||i==g.first||i==g.second;v.background=if(shown)RelicDrawable(this,g.cards[i],g.matched[i]==1)else CardBackDrawable(this);v.contentDescription="Card ${i+1}, ${if(shown)names[g.cards[i]]+if(g.matched[i]==1)", matched"else ", revealed"else "face down"}"};t.refresh()}
 fun hideLater(){handler.postDelayed({if(game===g&&g.second>=0){g.hide();persist();refresh()}},1100)}
 val all=mutableListOf<TextView>();repeat(g.cards.size){i->val v=TextView(this).apply{setPadding(0,0,0,0);isFocusable=true;isClickable=true}
  v.setOnClickListener{if(!animating&&g.second<0&&g.matched[i]==0&&i!=g.first&&!g.won){g.flip(i);changed();haptic(v);if(motionEnabled()){animating=true;v.animate().scaleX(0f).setDuration(90).withEndAction{if(game===g){refresh();v.animate().scaleX(1f).setDuration(130).withEndAction{animating=false}.start()}else animating=false}.start()}else refresh();if(g.second>=0)hideLater()}}
  t.place(v,16f+i%4*94f,gridY+i/4*cardH,85f,cardH-11);all.add(v)
 };cells=all
 val footer=maxOf(654f,gridY+gridH+12)
 t.control("Choose hunt difficulty",null,70f,footer,253f,44f){AntiqueDialogs.Builder(this).setTitle("Choose your hunt").setItems(arrayOf("6 pairs · Discovery","8 pairs · Explorer","12 pairs · Curator","16 pairs · Master")){_,i->newGame(MemoryGame(pairs=listOf(6,8,12,16)[i]))}.show()}.apply{drawBody=true;showLabel=false}
 t.text({when(g.pairs){6->"Discovery";8->"Explorer";12->"Curator";else->"Master"}+"  ·  ${g.pairs} pairs  ▾"},78f,footer+2,237f,40f,16f)
 t.text({if(g.won)"All relics recovered! · score ${g.score}"else "—  Flip two cards. Find the pair.  —"},25f,footer+48,343f,31f,14f)
 refresh();if(g.second>=0)hideLater()
}
