package com.ramy.mindgame
import android.widget.LinearLayout
import com.ramy.mindgame.games.TilesGame
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp
internal fun MainActivity.tiles(g: TilesGame) {
    val host=boardHost!!;val progress=Theme.text(this,"",13f,Theme.mint);host.addView(progress)
    fun refresh(){val largest=g.board.maxOrNull()?:2;progress.text=listOf(512,1024,2048,4096).joinToString("  →  "){if(largest>=it)"$it ✓"else "$it"}}
    val board=TilesBoardView(this,g,{motionEnabled()},{changed();refresh()});host.addView(board,LinearLayout.LayoutParams(-1,resources.displayMetrics.widthPixels-dp(this,28)));refresh()
    host.addView(Theme.space(this,12));host.addView(Theme.text(this,"←  Swipe the board to merge  →",14f,Theme.muted));host.addView(Theme.button(this,"Undo last move"){if(g.undo()){changed();refresh();board.sync()}})
    val arrows=Theme.row(this);listOf("←","↑","→","↓").forEachIndexed{i,s->Theme.addWeighted(arrows,Theme.button(this,s){board.move(i)})};host.addView(arrows)
}
