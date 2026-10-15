package com.ramy.mindgame
import android.app.AlertDialog
import android.text.InputType
import android.widget.*
import com.ramy.mindgame.games.MinesGame
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp
internal fun MainActivity.mines(g: MinesGame) {
    val t=gameTable!!;val label=when{g.mineCount==10&&g.columns<=9&&g.rows<=9->"Beginner";g.mineCount==40&&g.columns==16&&g.rows==16->"Intermediate";g.mineCount==99&&g.columns==30&&g.rows==16->"Expert";else->"Custom"}
    fun custom(){val form=Theme.column(this);form.setPadding(dp(this,20),0,dp(this,20),0);val fields=listOf("Columns (8–30)" to g.columns,"Rows (8–30)" to g.rows,"Mines" to g.mineCount).map{(label,n)->form.addView(Theme.text(this,label,14f,Theme.darkInk));EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;setText("$n");form.addView(this)}};val dialog=AntiqueDialogs.Builder(this).setTitle("Custom minefield").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Play",null).create();dialog.setOnShowListener{AntiqueDialogs.decorate(dialog);dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{val c=fields[0].text.toString().toIntOrNull();val r=fields[1].text.toString().toIntOrNull();val m=fields[2].text.toString().toIntOrNull();if(c!=null&&r!=null&&m!=null&&c in 8..30&&r in 8..30&&m in 1..c*r-10){dialog.dismiss();newMines(c,r,m)}else fields[2].error="Use 8–30 rows/columns and 1 to cells − 10 mines"}};dialog.show()}
    fun choose(){AntiqueDialogs.Builder(this).setTitle("Choose your challenge").setItems(arrayOf("Beginner · 9×9 · 10 mines","Intermediate · 16×16 · 40 mines","Expert · 30×16 · 99 mines","Custom board")){_,i->when(i){0->newMines(9,9,10);1->newMines(16,16,40);2->newMines(30,16,99);else->custom()}}.show()}
    t.text({"Minesweeper"},143f,47f,194f,40f,25f,Theme.ink,true,android.graphics.Paint.Align.LEFT)
    t.control("Choose minefield difficulty",null,142f,86f,201f,35f){choose()}.showLabel=false
    t.text({"$label · ${g.mineCount} mines  ▾"},143f,88f,201f,25f,15f,Theme.ink,false,android.graphics.Paint.Align.LEFT)
    t.text({elapsedLabel()},148f,124f,94f,38f,21f)
    t.text({"Flags ${g.flags.sum()} / ${g.mineCount}"},248f,124f,118f,38f,16f)
    t.text({if(g.lost)"Game over · try a new board"else if(g.won)"Minefield cleared!"else "${g.columns} × ${g.rows} · first reveal is safe"},24f,168f,344f,23f,12f,Theme.darkInk)
    val field=MinefieldView(this,g,{markMode},{changed()});t.place(field,22f,195f,349f,349f)
    val reveal=t.control("Reveal","reveal",19f,590f,173f,43f){};reveal.showLabel=true
    val flag=t.control("Flag","flag",198f,590f,176f,43f){};flag.showLabel=true;flag.ink=Theme.darkInk
    fun modes(){reveal.selectedControl=!markMode;flag.selectedControl=markMode;t.refresh()}
    reveal.setOnClickListener{markMode=false;modes()};flag.setOnClickListener{markMode=true;modes()};modes()
    t.control("New board","new",22f,658f,85f,68f){restartGame()}.ink=Theme.darkInk
    t.text({"New board"},20f,726f,89f,24f,14f,Theme.darkInk)
    t.control("Zoom in","plus",122f,652f,74f,44f){field.zoomBy(1.25f)}.apply{drawBody=true}
    t.control("Zoom out","minus",122f,704f,74f,44f){field.zoomBy(.8f)}.apply{drawBody=true}
    val overview=MineOverview(this,g,field);field.viewChanged={overview.invalidate()};t.place(overview,226f,651f,144f,116f)
    t.text({"Hold to flag · drag to pan · pinch to zoom"},17f,767f,363f,18f,11f,Theme.darkInk)
}
