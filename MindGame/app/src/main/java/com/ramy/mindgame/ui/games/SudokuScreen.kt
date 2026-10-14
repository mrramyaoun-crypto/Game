package com.ramy.mindgame
import android.app.AlertDialog
import android.graphics.Paint
import com.ramy.mindgame.games.SudokuGame
import com.ramy.mindgame.ui.*
internal fun MainActivity.sudoku(g:SudokuGame){
 val t=gameTable!!;var pencil=false
 t.text({"Sudoku"},145f,44f,185f,45f,30f,Theme.ink,true,Paint.Align.LEFT)
 t.control("Choose Sudoku difficulty",null,145f,86f,195f,43f){AlertDialog.Builder(this).setTitle("Choose difficulty").setItems(arrayOf("Easy · singles","Medium · locked candidates","Hard · candidate pairs","Expert · advanced deduction")){_,i->newGame(SudokuGame(difficulty=listOf("Easy","Medium","Hard","Expert")[i]))}.show()}.showLabel=false
 t.text({"${g.difficulty}  ▾  ·  ${elapsedLabel()}"},145f,90f,192f,30f,16f,Theme.ink,false,Paint.Align.LEFT)
 val board=SudokuBoardView(this,g,{selected},{i->selected=i;t.refresh()});t.place(board,18f,161f,361f,401f)
 listOf("Undo" to "undo","Erase" to "erase","Notes" to "notes","Hint" to "hint").forEachIndexed{i,(label,icon)->val control=t.control(label,icon,15f+i*97f,589f,73f,51f){
  when(i){0->if(g.undo())changed();1->if(selected>=0&&g.fixed[selected]==0){g.enter(selected,0);changed()};2->pencil=!pencil;3->{val index=g.hint();if(index>=0){selected=index;changed()}}};t.refresh()
 };if(i==2)control.setOnClickListener{pencil=!pencil;control.selectedControl=pencil;haptic(control);t.refresh()};t.text({label},12f+i*97f,643f,79f,23f,13f)}
 repeat(9){i->val n=i+1;t.control("Enter $n",null,7f+i*42.1f,693f,42.1f,70f){if(selected>=0&&g.fixed[selected]==0&&!g.won){if(pencil)g.note(selected,n)else g.enter(selected,n);changed();haptic(board);board.invalidate()}}.showLabel=false;t.text({"$n"},7f+i*42.1f,696f,42.1f,64f,29f,Theme.darkInk)}
 t.text({gameMessage().ifBlank{"Tap a cell · use Notes for possibilities"}},15f,764f,363f,21f,11f,Theme.darkInk)
}
