package com.ramy.mindgame

import android.content.Intent
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.*
import android.widget.*
import com.ramy.mindgame.core.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.ui.Theme.dp
import kotlin.math.abs

class MainActivity: Activity() {
    private lateinit var store: LocalStore
    internal var game: Game?=null
    private lateinit var root: LinearLayout
    private lateinit var screen: LinearLayout
    internal var boardHost: LinearLayout?=null
    internal var gameTable: GameTable?=null
    private var status: TextView?=null
    private var timer: TextView?=null
    private var celebration: android.animation.ValueAnimator?=null
    private var elapsed=0L
    private var runningSince=0L
    private var active=false
    private var recorded=false
    internal var selected=-1
    internal var markMode=false
    internal var codeInput=IntArray(4) {-1}
    internal var cells=listOf<TextView>()
    internal val handler=Handler(Looper.getMainLooper())
    private val tick=object: Runnable { override fun run() { timer?.text="${time(totalTime())}  ·  ${game?.moves ?: 0} moves"; gameTable?.refresh(); if(active) handler.postDelayed(this,1000) } }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); store=LocalStore(this)
        root=Theme.column(this); root.background=MaterialDrawable(this)
        root.setOnApplyWindowInsetsListener { v,i -> v.setPadding(i.systemWindowInsetLeft,i.systemWindowInsetTop,i.systemWindowInsetRight,i.systemWindowInsetBottom); i }
        setContentView(root); root.requestApplyInsets(); home()
    }
    override fun onResume() { super.onResume(); if(root.childCount>0 && root.getChildAt(0) is ExplorerDesk)home(); active=true; runningSince=SystemClock.elapsedRealtime(); handler.post(tick) }
    override fun onPause() { celebration?.cancel(); if(active && game!=null) elapsed=totalTime(); active=false; persist(); handler.removeCallbacks(tick); super.onPause() }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
    private fun totalTime()=elapsed+if(active && game!=null && !isFinished()) SystemClock.elapsedRealtime()-runningSince else 0L
    private fun isFinished()=game?.let { it.lost || (it.won && it !is TilesGame) } ?: false
    private fun time(ms: Long)="%d:%02d".format(ms/60000,ms/1000%60)
    internal fun motionEnabled() = store.motion
    internal fun pop(v: View) { if(store.motion) { v.scaleX=.85f; v.scaleY=.85f; v.animate().scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(android.view.animation.OvershootInterpolator()).start() } }
    internal fun haptic(v: View) { if(store.haptics) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    private fun page() {
        root.removeAllViews(); status=null; timer=null; boardHost=null
        val scroll=ScrollView(this); scroll.isFillViewport=true
        screen=Theme.column(this); screen.setPadding(dp(this,14),dp(this,10),dp(this,14),dp(this,20))
        scroll.addView(screen); root.addView(scroll,LinearLayout.LayoutParams(-1,-1))
    }
    private fun heading(title: String,subtitle: String) {
        screen.addView(Theme.text(this,title,30f,Theme.ink,true)); screen.addView(Theme.text(this,subtitle,14f,Theme.muted)); screen.addView(Theme.space(this,14))
    }
    private fun home() {
        persist();game=null;cells=emptyList();status=null;timer=null;boardHost=null;gameTable=null;root.removeAllViews()
        val id=store.recent().firstOrNull() ?: "nonogram"
        val j=store.read(id)
        val legacy=if(id=="enigma")try{getSharedPreferences("mindgame_v1",MODE_PRIVATE).getString("legacy_enigma_v13",null)?.let{org.json.JSONObject(it)}}catch(_:Exception){null}else null
        val hasSave=j!=null||legacy!=null
        val complete=j?.optBoolean("lost")==true||j?.optBoolean("won")==true&&id!="2048"||legacy?.optBoolean("solved")==true
        val name=if(id=="enigma")"Enigma"else Registry.get(id).name
        val detail=when {
            !hasSave->"Explore the desk above"
            id=="nonogram"->NonogramGame.pictures.firstOrNull{it.key==j?.optString("pictureKey")}?.name ?: "Continue your restoration"
            id=="sudoku"->"${j?.optString("difficulty","Easy")} · 9×9"
            id=="memory"->"${j?.optInt("pairs",8)} pairs · relic hunt"
            id=="mines"->"${j?.optInt("mineCount",10)} mines · ${j?.optInt("columns",9)}×${j?.optInt("rows",9)}"
            id=="mastermind"->"${j?.optInt("moves",0)} guesses made"
            id=="2048"->"Score ${j?.optInt("points",0)}"
            else->"${legacy?.optString("mode","normal")?.replaceFirstChar{it.uppercase()}} cipher"
        }
        val board=j?.optJSONArray("board");val progress=when {
            complete->1f
            id=="nonogram"&&board!=null->(0 until board.length()).count{board.optInt(it)>0}.toFloat()/board.length().coerceAtLeast(1)
            id=="memory"->j?.optJSONArray("matched")?.let{arr->(0 until arr.length()).count{arr.optInt(it)==1}.toFloat()/arr.length().coerceAtLeast(1)}
            else->null
        }
        val resume=DeskResume(id,name,detail,if(hasSave)time(j?.optLong("elapsed")?:legacy?.optLong("elapsed")?:0L)else "",progress,hasSave,if(complete)"Play again"else if(hasSave)"Resume"else "Explore")
        root.addView(ExplorerDesk(this,{store.motion},resume,{launch(it)},{settings()},{home()},{collection()},{statistics()},{haptic(it)}),LinearLayout.LayoutParams(-1,-1))
    }
    private fun collection() {
        persist();game=null;page();screen.addView(Theme.button(this,"‹ Explorer’s Desk") {home()});heading("Restoration collection","Solve mosaics to preserve them here.")
        val entries=NonogramGame.pictures
        entries.chunked(2).forEach {pair ->val row=Theme.row(this);pair.forEach {p->val card=Theme.column(this);card.background=Theme.paper(this);card.setPadding(dp(this,8),dp(this,8),dp(this,8),dp(this,8));val earned=store.collected(p.key);card.addView(MosaicPreview(this,p,earned),LinearLayout.LayoutParams(-1,dp(this,140)));card.addView(Theme.text(this,if(earned)p.name else "Unrestored · ${p.size}×${p.size}",14f,Theme.darkInk,true));card.isClickable=true;card.setOnClickListener {newGame(NonogramGame(pictureKey=p.key))};Theme.addWeighted(row,card,192)};screen.addView(row)}
    }
    private fun settings() {
        val labels=arrayOf("Gentle card animations","Touch feedback")
        AntiqueDialogs.Builder(this).setTitle("Settings").setMultiChoiceItems(labels,booleanArrayOf(store.motion,store.haptics)) { _,i,v -> if(i==0) store.motion=v else store.haptics=v }.setPositiveButton("Done") { _,_ -> home() }.show()
    }
    private fun statistics() {
        val content=Theme.column(this); content.setPadding(dp(this,20),dp(this,8),dp(this,20),dp(this,12))
        Registry.games.forEach { info -> val s=store.stat(info.id); content.addView(Theme.text(this,info.name,17f,0xFF205C53.toInt(),true)); content.addView(Theme.text(this,"${s.optInt("played")} started · ${s.optInt("wins")} wins · best score ${s.optInt("high")}\nBest time: ${if(s.optLong("best")==0L) "—" else time(s.optLong("best"))}",13f,Theme.darkInk)) }
        val scroll=ScrollView(this); scroll.addView(content); AntiqueDialogs.Builder(this).setTitle("Your statistics").setView(scroll).setPositiveButton("Done",null).show()
    }
    private fun launch(id: String,fresh: Boolean=false) {
        if(fresh && game is SudokuGame && id=="sudoku") {newGame(SudokuGame(difficulty=(game as SudokuGame).difficulty));return}
        if(fresh && game is MemoryGame && id=="memory") {newGame(MemoryGame(pairs=(game as MemoryGame).pairs));return}
        if(fresh && game is NonogramGame && id=="nonogram") {val old=game as NonogramGame;val pool=NonogramGame.pictures.filter{it.size==(if(old.size in listOf(10,15,20))old.size else 10)&&it.key!=old.pictureKey};newGame(NonogramGame(pictureKey=pool.random().key));return}
        if(id=="mines" && fresh && game is MinesGame) {val m=game as MinesGame;newMines(m.columns,m.rows,m.mineCount);return}
        if(id=="enigma") { persist(); store.touch(id); startActivity(Intent().setClassName(packageName,"com.codebreaker.game.EnigmaActivity")); return }
        persist(); val info=Registry.get(id); val saved=if(fresh) null else store.read(id)
        game=if(saved!=null) try { info.load(saved) } catch(_: Exception) { null } else null
        if(game==null || game!!.lost || (game!!.won && id!="2048")) { game=if(id=="mines" && saved!=null) MinesGame(columns=saved.optInt("columns",8),rows=saved.optInt("rows",8),mineCount=saved.optInt("mineCount",10)) else info.create(); elapsed=0L; recorded=false; store.start(id) } else { elapsed=saved!!.optLong("elapsed"); recorded=saved.optBoolean("recorded") }
        runningSince=SystemClock.elapsedRealtime(); selected=-1; markMode=false; codeInput=IntArray(4) {-1}; store.touch(id); persist(); playPage()
    }
    internal fun newGame(next: Game) {persist();game=next;elapsed=0;runningSince=SystemClock.elapsedRealtime();recorded=false;selected=-1;markMode=false;codeInput=IntArray(4){-1};store.start(next.id);store.touch(next.id);persist();playPage()}
    internal fun bestScore(id: String)=store.stat(id).optInt("high")
    internal fun newMines(columns: Int,rows: Int,count: Int) { persist();game=MinesGame(columns=columns,rows=rows,mineCount=count);elapsed=0;runningSince=SystemClock.elapsedRealtime();recorded=false;markMode=false;store.start("mines");persist();playPage() }
    internal fun playPage() {
        val g=game ?: return
        root.removeAllViews();status=null;timer=null;boardHost=null;cells=emptyList()
        val scroll=ScrollView(this).apply{isFillViewport=true;isVerticalScrollBarEnabled=false}
        val table=GameTable(this,g.id);gameTable=table;scroll.addView(table,FrameLayout.LayoutParams(-1,-2));root.addView(scroll,LinearLayout.LayoutParams(-1,-1))
        val back=table.control("Explorer’s Desk","back",7f,5f,48f,48f){home()}
        val menu=table.control("Game menu and rules","gear",337f,5f,48f,48f){AntiqueDialogs.Builder(this).setTitle(Registry.get(g.id).name).setItems(arrayOf("How to play","New game")){_,i->if(i==0)RuleGuide.show(this,g.id)else restartGame()}.show()}
        back.drawBody=true;menu.drawBody=true
        when(g){is SudokuGame->sudoku(g);is MinesGame->mines(g);is MemoryGame->memory(g);is TilesGame->tiles(g);is NonogramGame->nonogram(g);is CodeGame->code(g)}
        back.bringToFront();menu.bringToFront()
        scroll.setOnScrollChangeListener { _,_,y,_,_ -> back.translationY=y.toFloat();menu.translationY=y.toFloat() }
        table.refresh()
    }
    internal fun elapsedLabel()=time(totalTime())
    internal fun restartGame(){val g=game?:return;AntiqueDialogs.Builder(this).setTitle("Start a new game?").setMessage("This replaces your saved ${Registry.get(g.id).name} game.").setNegativeButton("Keep playing",null).setPositiveButton("New game"){_,_->launch(g.id,true)}.show()}
    internal fun isCollected(key:String)=store.collected(key)
    internal fun gameMessage():String{val g=game?:return "";return when{g.lost->"Game over · open menu for a new game";g.won&&g !is TilesGame->"Solved! · score ${g.score}";else->""}}
    internal fun persist() { game?.let { store.save(it,totalTime(),recorded) } }
    internal fun changed() {
        val g=game ?: return
        if(!recorded && (g.won || g.lost)) { val final=elapsed+if(active) SystemClock.elapsedRealtime()-runningSince else 0L; elapsed=final; runningSince=SystemClock.elapsedRealtime(); store.finish(g,final); if(g is NonogramGame && g.won)store.collect(g.pictureKey); recorded=true;if(g.won && store.motion) { celebration?.cancel();celebration=WinBurst.show(root) } }
        store.high(g.id,g.score); persist(); updateStatus();gameTable?.refresh(); timer?.text="${time(totalTime())}  ·  ${g.moves} moves"
    }
    private fun updateStatus() {
        val g=game ?: return
        status?.text=when { g.lost -> if(g is CodeGame) "Code revealed below · try again" else "Game over · try another puzzle"; g.won -> if(g is TilesGame) "2048 reached! Keep going · score ${g.score}" else "Solved!  ✦  Score ${g.score}"; else -> when(g) { is TilesGame -> "Score ${g.score} · best ${store.stat(g.id).optInt("high")}"; is MinesGame -> "Flags ${g.flags.sum()} / ${g.mineCount}"; is MemoryGame -> "${g.matched.sum()/2} / ${g.pairs} pairs matched"; is CodeGame -> "${10-g.moves} attempts remaining"; else -> "" } }
        status?.visibility=if(status?.text.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    internal fun grid(n: Int,labels: (Int)->String,colors: (Int)->Int,click: (Int)->Unit,longClick: ((Int)->Unit)?=null): List<TextView> {
        val host=boardHost ?: return emptyList(); val size=(resources.displayMetrics.widthPixels-dp(this,28))/n; val result=mutableListOf<TextView>()
        repeat(n) { r -> val row=Theme.row(this); repeat(n) { c -> val i=r*n+c; val cell=Theme.text(this,labels(i),if(n>=8) 19f else 28f,Theme.ink,true); cell.gravity=Gravity.CENTER; cell.setPadding(0,0,0,0); cell.background=Theme.shape(colors(i),dp(this,if(n>=8) 4 else 12).toFloat()); cell.isClickable=true; cell.isFocusable=true; cell.contentDescription="Row ${r+1}, column ${c+1}, ${labels(i).ifEmpty { "empty" }}"; cell.setOnClickListener { haptic(it); click(i) }; if(longClick!=null) cell.setOnLongClickListener { haptic(it); longClick(i); true }; row.addView(cell,LinearLayout.LayoutParams(0,size,1f).apply { setMargins(dp(this@MainActivity,if(n<=5) 3 else 1),dp(this@MainActivity,if(n<=5) 3 else 1),dp(this@MainActivity,if(n<=5) 3 else 1),dp(this@MainActivity,if(n<=5) 3 else 1)) }); result.add(cell) }; host.addView(row) }
        return result
    }


    internal val symbols=listOf("◆","●","▲","★","♥","✚","☀","☾")




    @Deprecated("Android back compatibility") override fun onBackPressed() { if(game!=null) home() else super.onBackPressed() }
}
