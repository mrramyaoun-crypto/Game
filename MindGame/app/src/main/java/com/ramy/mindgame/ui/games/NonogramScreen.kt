package com.ramy.mindgame
import android.app.AlertDialog
import android.widget.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp
internal fun MainActivity.nonogram(g: NonogramGame) {
    val host=boardHost!!;val pick=Theme.row(this);pick.addView(Theme.text(this,g.title,18f,Theme.ink,true),LinearLayout.LayoutParams(0,-2,1f));pick.addView(Theme.button(this,"${g.size}×${g.size} ▾"){val ps=NonogramGame.pictures;AlertDialog.Builder(this).setTitle("Choose a restoration").setItems(ps.map{"${it.size}×${it.size} · ${it.name}"}.toTypedArray()){_,i->newGame(NonogramGame(pictureKey=ps[i].key))}.show()});host.addView(pick)
    var completion: ()->Unit={}
    var pan=false;val field=MosaicView(this,g,{markMode},{pan},{changed();completion()});host.addView(field,LinearLayout.LayoutParams(-1,dp(this,400)))
    val tools=Theme.row(this);val fill=Theme.button(this,"Fill"){};val cross=Theme.button(this,"Mark ×"){};val panButton=Theme.button(this,"Pan"){}
    fun modes(){fill.alpha=if(!pan&&!markMode)1f else .55f;cross.alpha=if(!pan&&markMode)1f else .55f;panButton.alpha=if(pan)1f else .55f}
    fill.setOnClickListener{pan=false;markMode=false;modes()};cross.setOnClickListener{pan=false;markMode=true;modes()};panButton.setOnClickListener{pan=!pan;modes()};listOf(fill,cross,panButton).forEach{Theme.addWeighted(tools,it)};modes();host.addView(tools)
    val actions=Theme.row(this);Theme.addWeighted(actions,Theme.button(this,"Undo"){if(g.undo()){changed();field.invalidate();completion()}});Theme.addWeighted(actions,Theme.button(this,"−"){field.zoomBy(.8f)});Theme.addWeighted(actions,Theme.button(this,"+"){field.zoomBy(1.25f)});host.addView(actions);host.addView(Theme.text(this,"Drag to paint · pinch to zoom · Pan to explore",12f,Theme.muted))
    val p=NonogramGame.pictures.firstOrNull{it.key==g.pictureKey}
    if(p!=null){val restored=Theme.column(this);restored.addView(Theme.text(this,"RESTORED · added to your collection",12f,Theme.mint));restored.addView(MosaicPreview(this,p,true),LinearLayout.LayoutParams(-1,dp(this,300)));host.addView(restored,2);completion={field.visibility=if(g.won)android.view.View.GONE else android.view.View.VISIBLE;restored.visibility=if(g.won)android.view.View.VISIBLE else android.view.View.GONE;tools.visibility=if(g.won)android.view.View.GONE else android.view.View.VISIBLE};completion()}
}
