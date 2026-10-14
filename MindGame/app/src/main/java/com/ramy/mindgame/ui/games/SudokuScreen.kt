package com.ramy.mindgame
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.widget.LinearLayout
import com.ramy.mindgame.games.SudokuGame
import com.ramy.mindgame.ui.*
internal fun MainActivity.sudoku(g: SudokuGame) {
    val host=boardHost!!;var pencil=false
    host.addView(Theme.button(this,"${g.difficulty} · choose difficulty ▾") {AlertDialog.Builder(this).setTitle("Choose difficulty").setItems(arrayOf("Easy · singles","Medium · locked candidates","Hard · candidate pairs","Expert · advanced deduction")){_,i->newGame(SudokuGame(difficulty=listOf("Easy","Medium","Hard","Expert")[i]))}.show()});host.addView(Theme.space(this,10))
    fun refresh(){cells.forEachIndexed{i,v->val hasNotes=g.board[i]==0&&g.notes[i]!=0;v.text=if(g.board[i]!=0)"${g.board[i]}" else if(hasNotes)(1..9).chunked(3).joinToString("\n"){row->row.joinToString(" "){n->if(g.notes[i] and(1 shl n)!=0)"$n" else " "}}else "";v.textSize=if(hasNotes)8f else 22f;v.typeface=Typeface.create(if(hasNotes)"monospace" else "sans-serif",if(g.fixed[i]==1)Typeface.BOLD else Typeface.NORMAL);v.setTextColor(if(g.conflicts(i,g.board[i]))Color.parseColor("#BA3934")else if(g.fixed[i]==1)Theme.darkInk else Color.parseColor("#28786D"));v.background=SudokuCellDrawable(i,i==selected,selected>=0&&(i/9==selected/9||i%9==selected%9||i/27==selected/27&&i%9/3==selected%9/3||g.board[selected]>0&&g.board[i]==g.board[selected]),g.fixed[i]==1);v.contentDescription="Row ${i/9+1}, column ${i%9+1}, ${if(g.board[i]==0)"empty"else g.board[i]}, ${if(g.fixed[i]==1)"given"else "editable"}"}}
    cells=grid(9,{""},{Theme.surface},{i->selected=i;refresh()},{i->if(g.fixed[i]==0){g.enter(i,0);changed();refresh()}});cells.forEach{v->(v.layoutParams as LinearLayout.LayoutParams).setMargins(0,0,0,0);v.includeFontPadding=false};refresh();host.addView(Theme.space(this,12))
    val tools=Theme.row(this);val notes=Theme.button(this,"Notes"){};notes.alpha=.6f;notes.setOnClickListener{pencil=!pencil;notes.alpha=if(pencil)1f else .6f;notes.text=if(pencil)"Notes ✓"else "Notes";haptic(it)}
    Theme.addWeighted(tools,Theme.button(this,"Undo"){if(g.undo()){changed();refresh()}});Theme.addWeighted(tools,Theme.button(this,"Erase"){if(selected>=0&&g.fixed[selected]==0){g.enter(selected,0);changed();refresh()}});Theme.addWeighted(tools,notes);Theme.addWeighted(tools,Theme.button(this,"Hint"){val i=g.hint();if(i>=0){selected=i;changed();refresh();android.widget.Toast.makeText(this,"${g.solution[i]} revealed at row ${i/9+1}, column ${i%9+1}",android.widget.Toast.LENGTH_SHORT).show()}});host.addView(tools)
    listOf(1..5,6..9).forEach{range->val row=Theme.row(this);range.forEach{n->val key=Theme.button(this,"$n"){if(selected>=0&&g.fixed[selected]==0){if(pencil)g.note(selected,n)else g.enter(selected,n);changed();refresh();pop(cells[selected])}};key.textSize=24f;key.background=Theme.paper(this);key.setTextColor(Theme.darkInk);Theme.addWeighted(row,key,54)};host.addView(row)}
    host.addView(Theme.text(this,"Tap a cell · choose a number · Notes for possibilities",12f,Theme.muted))
}
