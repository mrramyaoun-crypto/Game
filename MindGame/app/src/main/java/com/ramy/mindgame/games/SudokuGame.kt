package com.ramy.mindgame.games
import com.ramy.mindgame.core.*
import org.json.JSONObject
import kotlin.random.Random

class SudokuGame(random: Random=Random.Default,val difficulty: String="Easy"): Game {
    override val id="sudoku"; override var moves=0; override var won=false; override var lost=false
    var solution=IntArray(81); var board=IntArray(81); var fixed=IntArray(81); var notes=IntArray(81)
    private val history=ArrayDeque<Pair<IntArray,IntArray>>()
    init {
        val puzzle=SudokuCatalog.puzzles.getValue(difficulty).random(random)
        val digits=(1..9).shuffled(random)
        val rows=(0..2).shuffled(random).flatMap{b->(0..2).shuffled(random).map{b*3+it}}
        val cols=(0..2).shuffled(random).flatMap{b->(0..2).shuffled(random).map{b*3+it}}
        val transpose=random.nextBoolean()
        fun transform(raw: String)=IntArray(81){i->val r=if(transpose)i%9 else i/9;val c=if(transpose)i/9 else i%9;val v=raw[rows[r]*9+cols[c]]-'0';if(v==0)0 else digits[v-1]}
        board=transform(puzzle.first);solution=transform(puzzle.second);fixed=IntArray(81){if(board[it]!=0)1 else 0}
    }
    private fun remember() {history.addLast(board.copyOf() to notes.copyOf());if(history.size>40)history.removeFirst()}
    fun undo():Boolean {if(history.isEmpty())return false;val h=history.removeLast();board=h.first;notes=h.second;won=false;moves++;return true}
    fun hint():Int {val i=board.indices.firstOrNull{board[it]==0} ?: return -1;enter(i,solution[i]);return i}
    fun conflicts(i: Int, v: Int)=v!=0 && board.indices.any { it!=i && board[it]==v && (it/9==i/9 || it%9==i%9 || (it/27==i/27 && it%9/3==i%9/3)) }
    fun enter(i: Int, v: Int) { if(!won && fixed[i]==0 && v in 0..9) { if(board[i]==v&&notes[i]==0)return;remember();board[i]=v; notes[i]=0; moves++; won=board.all { it!=0 } && board.indices.none { conflicts(it,board[it]) } } }
    fun note(i: Int, n: Int) { if(!won && fixed[i]==0 && board[i]==0 && n in 1..9) { remember();notes[i]=notes[i] xor (1 shl n) } }
    override val score get()=if(won) 1000 else board.indices.count { fixed[it]==0 && board[it]!=0 }*10
    override fun save()=base().put("difficulty",difficulty).put("undo",org.json.JSONArray(history.map{org.json.JSONObject().put("board",it.first.json()).put("notes",it.second.json())})).put("solution",solution.json()).put("board",board.json()).put("fixed",fixed.json()).put("notes",notes.json())
    companion object {
        fun countSolutions(input: IntArray, limit: Int=2): Int {
            val b=input.copyOf(); var total=0
            fun candidates(i: Int)=(1..9).filter { v -> b.indices.none { it!=i && b[it]==v && (it/9==i/9 || it%9==i%9 || (it/27==i/27 && it%9/3==i%9/3)) } }
            fun solve() {
                if(total>=limit) return
                var best=-1; var options: List<Int> = emptyList()
                for(i in b.indices) if(b[i]==0) { val c=candidates(i); if(c.isEmpty()) return; if(best<0 || c.size<options.size) { best=i; options=c }; if(c.size==1) break }
                if(best<0) { total++; return }
                for(v in options) { b[best]=v; solve(); if(total>=limit) break }; b[best]=0
            }
            if(b.indices.any { i -> b[i]!=0 && b.indices.any { k -> k!=i && b[k]==b[i] && (k/9==i/9 || k%9==i%9 || (k/27==i/27 && k%9/3==i%9/3)) } }) return 0
            solve(); return total
        }
        fun load(j: JSONObject)=SudokuGame(difficulty=j.optString("difficulty","Easy")).apply { restoreBase(j); solution=j.ints("solution"); board=j.ints("board"); fixed=j.ints("fixed"); notes=if(j.has("notes")) j.ints("notes") else IntArray(81);j.optJSONArray("undo")?.let{a->repeat(a.length()){k->val h=a.getJSONObject(k);history.addLast(h.ints("board") to h.ints("notes"))}} }
    }
}
