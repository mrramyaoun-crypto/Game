package com.ramy.mindgame.games
import com.ramy.mindgame.core.*
import org.json.JSONObject
import kotlin.random.Random

class MemoryGame(random: Random = Random.Default, val pairs: Int=8): Game {
    override val id="memory"; override var moves=0; override var won=false; override var lost=false
    init {require(pairs in listOf(6,8,12,16))}
    var cards=(0 until pairs).flatMap { listOf(it,it) }.shuffled(random).toIntArray()
    var matched=IntArray(pairs*2); var first=-1; var second=-1
    fun flip(i: Int) {
        if(won || matched[i]==1 || i==first || second>=0) return
        if(first<0) first=i else { second=i; moves++; if(cards[first]==cards[second]) { matched[first]=1; matched[second]=1; first=-1; second=-1; won=matched.all { it==1 } } }
    }
    fun hide() { first=-1; second=-1 }
    override val score get() = if(won) maxOf(0,2000-moves*50) else matched.sum()*50
    override fun save() = base().put("pairs",pairs).put("cards",cards.json()).put("matched",matched.json()).put("first",first).put("second",second)
    companion object { fun load(j: JSONObject)=MemoryGame(pairs=j.optInt("pairs",j.getJSONArray("cards").length()/2)).apply { restoreBase(j); cards=j.ints("cards"); matched=j.ints("matched"); first=j.getInt("first"); second=j.getInt("second") } }
}
