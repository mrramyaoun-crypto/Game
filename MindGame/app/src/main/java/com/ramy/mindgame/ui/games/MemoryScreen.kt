package com.ramy.mindgame
import android.app.AlertDialog
import android.view.Gravity
import android.widget.*
import com.ramy.mindgame.games.MemoryGame
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp

internal fun MainActivity.memory(g: MemoryGame) {
    val names=listOf("compass","key","hourglass","telescope","coin","wax seal","quill","map","pocket watch","scarab","sextant","lamp","chest","book","crystal vial","feather")
    val host=boardHost!!;host.addView(Theme.button(this,"${g.pairs} pairs · choose hunt ▾") {AlertDialog.Builder(this).setTitle("Choose your hunt").setItems(arrayOf("6 pairs · discovery","8 pairs · explorer","12 pairs · curator","16 pairs · master")){_,i->newGame(MemoryGame(pairs=listOf(6,8,12,16)[i]))}.show()})
    host.addView(Theme.space(this,10));var animating=false
    fun refresh() {cells.forEachIndexed{i,v->val shown=g.matched[i]==1||i==g.first||i==g.second;v.background=if(shown)RelicDrawable(this,g.cards[i],g.matched[i]==1) else PieceDrawable("memory-back");v.contentDescription="Card ${i+1}, ${if(shown)names[g.cards[i]]+if(g.matched[i]==1)", matched" else ", revealed" else "face down"}"}}
    fun hideLater() {handler.postDelayed({if(game===g&&g.second>=0){g.hide();persist();refresh()}},1100)}
    val columns=4;val size=minOf((resources.displayMetrics.widthPixels-dp(this,28))/columns,dp(this,if(g.pairs==16)64 else if(g.pairs==12)76 else 90));val all=mutableListOf<TextView>()
    repeat((g.cards.size+columns-1)/columns){r->val row=Theme.row(this);row.gravity=Gravity.CENTER;repeat(columns){c->val i=r*columns+c;if(i<g.cards.size){val v=Theme.text(this,"",0f);v.setPadding(0,0,0,0);v.isFocusable=true;v.setOnClickListener {if(!animating&&g.second<0&&g.matched[i]==0&&i!=g.first&&!g.won){g.flip(i);changed();haptic(v);if(motionEnabled()){animating=true;v.animate().scaleX(0f).setDuration(100).withEndAction{if(game===g){refresh();v.animate().scaleX(1f).setDuration(140).withEndAction{animating=false}.start()}else animating=false}.start()}else refresh();if(g.second>=0)hideLater()}};row.addView(v,LinearLayout.LayoutParams(size,size).apply{setMargins(2,2,2,2)});all.add(v)}else row.addView(android.view.View(this),LinearLayout.LayoutParams(size,size))};host.addView(row)}
    cells=all;refresh();if(g.second>=0)hideLater();host.addView(Theme.text(this,"Find the relics · remember their places",13f,Theme.muted))
}
