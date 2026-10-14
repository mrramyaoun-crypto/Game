package com.ramy.mindgame
import android.app.AlertDialog
import android.graphics.Paint
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
internal fun MainActivity.nonogram(g:NonogramGame){
 val t=gameTable!!
 fun pick(){val ps=NonogramGame.pictures;AlertDialog.Builder(this).setTitle("Choose a restoration").setItems(ps.map{"${it.size}×${it.size} · ${it.name}"}.toTypedArray()){_,i->newGame(NonogramGame(pictureKey=ps[i].key))}.show()}
 t.text({"Mosaic Restoration"},57f,3f,278f,40f,22f,Theme.ink,true)
 t.control("Choose restoration and size",null,147f,49f,192f,74f){pick()}.showLabel=false
 t.text({g.title},146f,60f,187f,30f,20f,Theme.ink,true,Paint.Align.LEFT)
 t.text({"${g.size} × ${g.size}  ▾  ·  ${elapsedLabel()}"},146f,90f,188f,22f,13f,Theme.ink,false,Paint.Align.LEFT)
 t.place(HuntProgress(this,{g.board.indices.count{g.board[it]==1}.toFloat()/g.solution.count{it==1}.coerceAtLeast(1)}),148f,116f,174f,8f)
 var pan=false;val field=MosaicView(this,g,{markMode},{pan},{changed();if(g.won)playPage()else t.refresh()});t.place(field,26f,145f,338f,365f)
 val fill=t.control("Fill","fill",93f,548f,99f,44f){markMode=false;pan=false;t.refresh()};fill.showLabel=true
 val mark=t.control("Mark ×","cross",212f,548f,99f,44f){markMode=true;pan=false;t.refresh()};mark.showLabel=true
 t.control("Undo","undo",30f,548f,46f,44f){if(g.undo()){changed();field.invalidate()}}
 t.control("Zoom in","zoom",315f,548f,45f,44f){field.zoomBy(1.25f)}
 t.control("Zoom out","minus",307f,594f,43f,35f){field.zoomBy(.8f)}
 t.control("Pan",null,48f,594f,55f,35f){pan=!pan;markMode=false;t.refresh()}
 t.text({if(g.won)"Restored! · added to your collection"else if(pan)"Pan mode · drag to explore"else if(markMode)"Mark empty tiles · pinch to zoom"else "Drag to fill · pinch to zoom"},104f,596f,194f,29f,11f)
 t.text({"Restoration collection"},25f,631f,240f,27f,16f,Theme.darkInk,true,Paint.Align.LEFT)
 val ps=NonogramGame.pictures;val collection=listOf((ps.firstOrNull{it.key==g.pictureKey}?:ps.first()))+ps.filter{it.key!=g.pictureKey}.take(2)
 collection.forEachIndexed{i,p->val v=MosaicPreview(this,p,isCollected(p.key)||g.won&&g.pictureKey==p.key);v.setOnClickListener{newGame(NonogramGame(pictureKey=p.key))};t.place(v,34f+i*111f,668f,100f,94f)}
}
