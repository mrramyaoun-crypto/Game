package com.ramy.mindgame.games
import com.ramy.mindgame.core.*
import org.json.JSONObject
import kotlin.random.Random

class TilesGame(private val random: Random=Random.Default): Game {
    override val id="2048"; override var moves=0; override var won=false; override var lost=false
    private var previous: org.json.JSONObject?=null
    fun undo():Boolean {val j=previous ?: return false;board=j.ints("board");points=j.getInt("points");moves=j.getInt("moves");won=j.getBoolean("won");lost=j.getBoolean("lost");previous=null;return true}
    var board=IntArray(16); var points=0; override val score get()=points
    init { spawn(); spawn() }
    fun spawn() { val empty=board.indices.filter { board[it]==0 }; if(empty.isNotEmpty()) board[empty.random(random)]=if(random.nextInt(10)==0) 4 else 2 }
    // 0 left, 1 up, 2 right, 3 down. Tiles merge once per move.
    fun slide(direction: Int): Boolean {
        if(lost) return false
        val snapshot=base().put("board",board.json()).put("points",points)
        val before=board.copyOf()
        repeat(4) { line ->
            val indices=IntArray(4) { p -> when(direction) { 0->line*4+p; 1->p*4+line; 2->line*4+3-p; else->(3-p)*4+line } }
            val values=indices.map { board[it] }.filter { it!=0 }; val output=mutableListOf<Int>(); var p=0
            while(p<values.size) { if(p+1<values.size && values[p]==values[p+1]) { val n=values[p]*2; output.add(n); points+=n; p+=2 } else { output.add(values[p]); p++ } }
            indices.forEachIndexed { k,i -> board[i]=output.getOrElse(k) { 0 } }
        }
        if(before.contentEquals(board)) return false
        previous=snapshot;moves++; spawn(); won=board.any { it>=2048 }; lost=!canMove(); return true
    }
    fun canMove()=board.any { it==0 } || board.indices.any { i -> (i%4<3 && board[i]==board[i+1]) || (i/4<3 && board[i]==board[i+4]) }
    override fun save()=base().put("board",board.json()).put("points",points).put("previous",previous?:org.json.JSONObject.NULL)
    companion object { fun load(j: JSONObject)=TilesGame().apply { restoreBase(j); board=j.ints("board"); points=j.getInt("points");previous=j.optJSONObject("previous") } }
}
