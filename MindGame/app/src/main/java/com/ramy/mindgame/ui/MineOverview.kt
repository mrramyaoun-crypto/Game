package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.*
import android.view.View
import com.ramy.mindgame.games.MinesGame
class MineOverview(c:Context,private val g:MinesGame,private val field:MinefieldView):View(c){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    init{contentDescription="Minefield overview, outlined area is visible on the board"}
    override fun onDraw(c:Canvas){val sx=width.toFloat()/g.columns;val sy=height.toFloat()/g.rows;g.open.indices.forEach{i->p.color=if(g.flags[i]==1)Theme.brass else if(g.open[i]==1)0xFFF0DEB9.toInt()else 0xFF9C855D.toInt();c.drawRect(i%g.columns*sx,i/g.columns*sy,(i%g.columns+1)*sx-.5f,(i/g.columns+1)*sy-.5f,p)};val v=field.viewport();p.color=Theme.mint;p.style=Paint.Style.STROKE;p.strokeWidth=2f;c.drawRect(v.left*width,v.top*height,v.right*width,v.bottom*height,p);p.style=Paint.Style.FILL}
}
