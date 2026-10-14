package com.ramy.mindgame
import com.ramy.mindgame.games.TilesGame
import com.ramy.mindgame.ui.*
internal fun MainActivity.tiles(g:TilesGame){
 val t=gameTable!!
 t.text({"2048"},150f,40f,161f,44f,34f,Theme.ink,true);t.text({"2048"},51f,81f,94f,37f,26f,Theme.ink,true)
 t.text({"Score"},165f,97f,97f,24f,15f);t.text({"${g.points}"},165f,119f,97f,26f,22f)
 t.text({"Best"},280f,97f,94f,24f,15f);t.text({"${bestScore(g.id)}"},280f,119f,94f,26f,22f)
 t.place(TileMilestones(this,{g.board.maxOrNull()?:2}),40f,176f,317f,39f)
 val board=TilesBoardView(this,g,{motionEnabled()},{changed();t.refresh()});t.place(board,18f,239f,358f,358f)
 t.text({if(g.lost)"No moves left · start a new game"else if(g.won)"2048 reached! Keep merging."else "←     Swipe to merge     →"},28f,630f,337f,36f,17f)
 t.control("Undo","undo",20f,685f,99f,59f){if(g.undo()){changed();board.sync()}}
 t.control("New game","new",140f,685f,232f,60f){restartGame()}.showLabel=true
 t.text({"Undo"},25f,747f,91f,24f,13f);t.text({"${elapsedLabel()} · ${g.moves} moves"},150f,756f,222f,21f,12f)
}
