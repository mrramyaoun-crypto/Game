package com.ramy.mindgame
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp

internal fun MainActivity.code(g: CodeGame) {
    codeInput=g.draft
    val host=boardHost!!;val colors=listOf("#C64D42","#E5B63F","#3A9C7E","#429CC3","#9371B2","#E08A40").map(Color::parseColor)
    val names=listOf("red","gold","green","blue","violet","orange")
    var slot=codeInput.indexOf(-1).coerceAtLeast(0)
    val board=Theme.column(this);board.background=MaterialDrawable(this);board.setPadding(dp(this,8),dp(this,8),dp(this,8),dp(this,8))
    val activePegs=mutableListOf<TextView>();val submit=Theme.button(this,"Submit guess"){}
    fun refresh() {activePegs.forEachIndexed {i,v->v.background=PieceDrawable("peg",if(codeInput[i]<0)0xFF241A11.toInt() else colors[codeInput[i]],matched=i==slot);v.contentDescription="Position ${i+1}, ${if(codeInput[i]<0) "empty" else names[codeInput[i]]}, tap to select";v.alpha=if(i==slot)1f else .8f};submit.isEnabled=codeInput.all {it>=0} && !g.won && !g.lost;submit.alpha=if(submit.isEnabled)1f else .5f}
    fun row(values: IntArray?,r: Int,active: Boolean=false): LinearLayout {
        val row=Theme.row(this);row.background=Theme.shape(0x663A200B,dp(this,8).toFloat(),0x775F4323)
        row.addView(Theme.text(this,if(active)"›" else "${r+1}",11f,Theme.muted).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(this,22),dp(this,if(active)44 else 32)))
        repeat(4) {i->val v=Theme.text(this,"",0f);v.setPadding(0,0,0,0);v.background=PieceDrawable("peg",if(values==null||values[i]<0)0xFF241A11.toInt() else colors[values[i]]);row.addView(v,LinearLayout.LayoutParams(0,dp(this,if(active)44 else 32),1f));if(active){activePegs.add(v);v.isFocusable=true;v.setOnClickListener {slot=i;refresh();haptic(v)}}else v.contentDescription="Guess ${r+1}, position ${i+1}, ${if(values==null)"empty" else names[values[i]]}"}
        if(!active) {val f=if(values==null)Feedback(0,0) else feedback(g.secret,values);row.addView(FeedbackPins(this,f.exact,f.misplaced),LinearLayout.LayoutParams(dp(this,38),dp(this,32)))}
        return row
    }
    repeat(10) {r->board.addView(row(g.attempts.getOrNull(r),r));board.addView(Theme.space(this,2))};host.addView(board)
    if(!g.won&&!g.lost) {
        host.addView(Theme.text(this,"YOUR GUESS · tap a slot to replace",11f,Theme.muted));host.addView(row(codeInput,0,true))
        val palette=Theme.row(this);repeat(6){i->val color=Theme.text(this,"",0f);color.background=PieceDrawable("peg",colors[i]);color.isFocusable=true;color.contentDescription="Choose ${names[i]}";color.setOnClickListener {codeInput[slot]=i;slot=codeInput.indexOf(-1).let{if(it>=0)it else (slot+1)%4};refresh();persist();pop(color);haptic(color)};Theme.addWeighted(palette,color,48)};host.addView(palette)
        submit.setOnClickListener {if(codeInput.all{it>=0}) {g.submit(codeInput);g.draft=IntArray(4){-1};codeInput=g.draft;changed();playPage()}};host.addView(submit);refresh()
    }
    host.addView(Theme.text(this,"Black = exact · ivory = right color elsewhere",12f,Theme.muted))
    if(g.won||g.lost){host.addView(Theme.text(this,"THE HIDDEN CODE",11f));host.addView(row(g.secret,0))}
}
