package com.ramy.mindgame.games
import com.ramy.mindgame.core.*
import org.json.JSONObject
import kotlin.random.Random

class MinesGame(private val random: Random = Random.Default, val columns: Int=9, val rows: Int=9, val mineCount: Int=10): Game {
    override val id = "mines"; override var moves = 0; override var won = false; override var lost = false
    init { require(columns in 8..30 && rows in 8..30 && mineCount in 1..(columns*rows-10)) }
    var mines = IntArray(columns*rows); var open = IntArray(columns*rows); var flags = IntArray(columns*rows); var planted = false
    fun neighbors(i: Int) = buildList { for(r in maxOf(0,i/columns-1)..minOf(rows-1,i/columns+1)) for(c in maxOf(0,i%columns-1)..minOf(columns-1,i%columns+1)) { val n=r*columns+c; if(n!=i) add(n) } }
    fun adjacent(i: Int) = neighbors(i).sumOf { mines[it] }
    fun flag(i: Int) { if (!won && !lost && open[i]==0) flags[i] = 1-flags[i] }
    fun reveal(i: Int) {
        if (won || lost || open[i]==1 || flags[i]==1) return
        if (!planted) { val safe=(neighbors(i)+i).toSet(); mines.indices.filter { it !in safe }.shuffled(random).take(mineCount).forEach { mines[it]=1 }; planted=true }
        moves++
        if (mines[i]==1) { open[i]=1; lost=true; return }
        val queue = ArrayDeque<Int>(); queue.add(i)
        while(queue.isNotEmpty()) {
            val p=queue.removeFirst()
            if(open[p]==1 || flags[p]==1) continue
            open[p]=1
            if(adjacent(p)==0) neighbors(p).filter { mines[it]==0 && open[it]==0 }.forEach { queue.add(it) }
        }
        won = mines.indices.all { mines[it]==1 || open[it]==1 }
    }
    fun chord(i: Int) {
        if(won||lost||open[i]==0||adjacent(i)==0)return
        if(neighbors(i).sumOf{flags[it]}==adjacent(i))neighbors(i).filter{flags[it]==0&&open[it]==0}.forEach{reveal(it)}
    }
    override val score get() = if(won) 1000 else open.sum()*10
    override fun save() = base().put("columns",columns).put("rows",rows).put("mineCount",mineCount).put("mines",mines.json()).put("open",open.json()).put("flags",flags.json()).put("planted",planted)
    companion object { fun load(j: JSONObject) = MinesGame(columns=j.optInt("columns",8),rows=j.optInt("rows",8),mineCount=j.optInt("mineCount",10)).apply { restoreBase(j); mines=j.ints("mines"); open=j.ints("open"); flags=j.ints("flags"); planted=j.getBoolean("planted") } }
}
