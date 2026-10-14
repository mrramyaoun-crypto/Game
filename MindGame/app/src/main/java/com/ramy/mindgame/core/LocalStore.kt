package com.ramy.mindgame.core
import android.content.Context
import org.json.JSONObject

class LocalStore(context: Context) {
    private val prefs=context.getSharedPreferences("mindgame_v1",Context.MODE_PRIVATE)
    var motion: Boolean get()=prefs.getBoolean("motion",true); set(v) { prefs.edit().putBoolean("motion",v).apply() }
    var haptics: Boolean get()=prefs.getBoolean("haptics",true); set(v) { prefs.edit().putBoolean("haptics",v).apply() }
    fun recent()=prefs.getString("recent","")!!.split(",").filter { id -> Registry.games.any { it.id==id } }
    fun touch(id: String) { prefs.edit().putString("recent",(listOf(id)+recent().filter { it!=id }).take(7).joinToString(",")).apply() }
    fun read(id: String): JSONObject? = try { prefs.getString("save_$id",null)?.let { JSONObject(it) } } catch(_: Exception) { null }
    fun save(game: Game,elapsed: Long, recorded: Boolean) { val j=game.save().put("elapsed",elapsed).put("recorded",recorded).put("schema",1); prefs.edit().putString("save_${game.id}",j.toString()).apply() }
    fun stat(id: String)=try { JSONObject(prefs.getString("stat_$id","{}")!!) } catch(_: Exception) { JSONObject() }
    fun start(id: String) { val j=stat(id); j.put("played",j.optInt("played")+1); prefs.edit().putString("stat_$id",j.toString()).apply() }
    fun finish(g: Game,elapsed: Long) {
        val j=stat(g.id); j.put("wins",j.optInt("wins")+if(g.won) 1 else 0); j.put("high",maxOf(j.optInt("high"),g.score))
        if(g.won && (j.optLong("best")==0L || elapsed<j.optLong("best"))) j.put("best",elapsed)
        prefs.edit().putString("stat_${g.id}",j.toString()).apply()
    }
    fun collected(key: String)=prefs.getBoolean("mosaic_$key",false)
    fun collect(key: String) {prefs.edit().putBoolean("mosaic_$key",true).apply()}
    fun high(id: String,score: Int) { val j=stat(id); if(score>j.optInt("high")) { j.put("high",score); prefs.edit().putString("stat_$id",j.toString()).apply() } }
}
