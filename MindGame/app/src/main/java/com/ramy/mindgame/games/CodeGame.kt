package com.ramy.mindgame.games

import com.ramy.mindgame.core.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class Feedback(val exact: Int, val misplaced: Int)
fun feedback(secret: IntArray, guess: IntArray): Feedback {
    val exact = secret.indices.count { secret[it] == guess[it] }
    val total = (0..9).sumOf { v -> minOf(secret.count { it == v }, guess.count { it == v }) }
    return Feedback(exact, total - exact)
}
class CodeGame(override val id: String, private val random: Random = Random.Default) : Game {
    override var moves = 0; override var won = false; override var lost = false
    var secret = IntArray(4) { random.nextInt(if (id == "enigma") 10 else 6) }
    var draft=IntArray(4){-1}
    val attempts = mutableListOf<IntArray>()
    val clues = mutableListOf<IntArray>()
    init { if (id == "enigma") makeClues() }
    private fun makeClues() {
        // Greedily narrow all 10,000 candidates; every generated puzzle has one solution.
        var candidates = (0..9999).map { n -> intArrayOf(n/1000,n/100%10,n/10%10,n%10) }
        while (candidates.size > 1 && clues.size < 15) {
            val guess = if (clues.size >= 12) secret.copyOf() else IntArray(4) { random.nextInt(10) }
            val f = feedback(secret, guess)
            val remaining = candidates.filter { feedback(it, guess) == f }
            if (remaining.size < candidates.size) { clues.add(guess); candidates = remaining }
        }
        if (candidates.size > 1) clues.add(secret.copyOf())
    }
    fun submit(guess: IntArray): Feedback {
        require(guess.size == 4 && guess.all { it in 0 until if (id == "enigma") 10 else 6 })
        val f = feedback(secret, guess)
        if (!won && !lost) { attempts.add(guess.copyOf()); moves++; won = f.exact == 4; lost = !won && moves >= 10 }
        return f
    }
    override val score get() = if (won) (11-moves)*100 else 0
    override fun save() = base().put("draft",draft.json()).put("secret",secret.json()).put("attempts",JSONArray(attempts.map { it.json() })).put("clues",JSONArray(clues.map { it.json() }))
    companion object {
        fun load(j: JSONObject): CodeGame = CodeGame(j.getString("id")).apply {
            restoreBase(j); secret = j.ints("secret");draft=if(j.has("draft"))j.ints("draft")else IntArray(4){-1}; attempts.clear(); clues.clear()
            j.getJSONArray("attempts").let { a -> repeat(a.length()) { i -> attempts.add(IntArray(4) { a.getJSONArray(i).getInt(it) }) } }
            j.getJSONArray("clues").let { a -> repeat(a.length()) { i -> clues.add(IntArray(4) { a.getJSONArray(i).getInt(it) }) } }
        }
    }
}
