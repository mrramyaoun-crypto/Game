package com.ramy.mindgame.core

import org.json.JSONArray
import org.json.JSONObject

interface Game {
    val id: String
    var moves: Int
    var won: Boolean
    var lost: Boolean
    val score: Int get() = 0
    fun save(): JSONObject
}
fun IntArray.json() = JSONArray(toList())
fun JSONObject.ints(key: String) = getJSONArray(key).let { a -> IntArray(a.length()) { a.getInt(it) } }
fun Game.base() = JSONObject().put("id", id).put("moves", moves).put("won", won).put("lost", lost)
fun Game.restoreBase(j: JSONObject) { moves = j.optInt("moves"); won = j.optBoolean("won"); lost = j.optBoolean("lost") }
