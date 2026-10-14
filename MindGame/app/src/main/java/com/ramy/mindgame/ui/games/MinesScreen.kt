package com.ramy.mindgame
import android.app.AlertDialog
import android.text.InputType
import android.widget.*
import com.ramy.mindgame.games.MinesGame
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp
internal fun MainActivity.mines(g: MinesGame) {
    val host=boardHost!!;val label=when{g.mineCount==10&&g.columns<=9&&g.rows<=9->"Beginner";g.mineCount==40&&g.columns==16&&g.rows==16->"Intermediate";g.mineCount==99&&g.columns==30&&g.rows==16->"Expert";else->"Custom"}
    fun custom(){val form=Theme.column(this);form.setPadding(dp(this,20),0,dp(this,20),0);val fields=listOf("Columns (8–30)" to g.columns,"Rows (8–30)" to g.rows,"Mines" to g.mineCount).map{(label,n)->form.addView(Theme.text(this,label,14f,Theme.ink));EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;setText("$n");form.addView(this)}};val dialog=AlertDialog.Builder(this).setTitle("Custom minefield").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Play",null).create();dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{val c=fields[0].text.toString().toIntOrNull();val r=fields[1].text.toString().toIntOrNull();val m=fields[2].text.toString().toIntOrNull();if(c!=null&&r!=null&&m!=null&&c in 8..30&&r in 8..30&&m in 1..c*r-10){dialog.dismiss();newMines(c,r,m)}else fields[2].error="Use 8–30 rows/columns and 1 to cells − 10 mines"}};dialog.show()}
    host.addView(Theme.button(this,"$label · ${g.columns}×${g.rows} · ${g.mineCount} mines ▾"){AlertDialog.Builder(this).setTitle("Choose your challenge").setItems(arrayOf("Beginner · 9×9 · 10 mines","Intermediate · 16×16 · 40 mines","Expert · 30×16 · 99 mines","Custom board")){_,i->when(i){0->newMines(9,9,10);1->newMines(16,16,40);2->newMines(30,16,99);else->custom()}}.show()});host.addView(Theme.space(this,10))
    val field=MinefieldView(this,g,{markMode},{changed()});host.addView(field,LinearLayout.LayoutParams(-1,resources.displayMetrics.widthPixels-dp(this,28)));host.addView(Theme.space(this,10))
    val tools=Theme.row(this);val reveal=Theme.button(this,"Reveal"){};val flag=Theme.button(this,"Flag"){};fun modes(){reveal.alpha=if(markMode).55f else 1f;flag.alpha=if(markMode)1f else .55f};reveal.setOnClickListener{markMode=false;modes()};flag.setOnClickListener{markMode=true;modes()};Theme.addWeighted(tools,reveal);Theme.addWeighted(tools,flag);modes();host.addView(tools)
    val zoom=Theme.row(this);Theme.addWeighted(zoom,Theme.button(this,"− Zoom"){field.zoomBy(.8f)});Theme.addWeighted(zoom,Theme.button(this,"+ Zoom"){field.zoomBy(1.25f)});host.addView(zoom);val footer=Theme.row(this);footer.addView(Theme.text(this,"Hold to flag · drag to pan\nPinch to zoom",12f,Theme.muted),LinearLayout.LayoutParams(0,-2,1f));if(g.columns>9){val overview=MineOverview(this,g,field);field.viewChanged={overview.invalidate()};footer.addView(overview,LinearLayout.LayoutParams(dp(this,100),dp(this,62)))};host.addView(footer)
}
