package com.ramy.mindgame

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.PieceDrawable
import com.ramy.mindgame.ui.MinefieldView
import android.view.MotionEvent
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RenderTests {
    private fun capture(activity: android.app.Activity,name: String) {
        val content=activity.findViewById<View>(android.R.id.content)
        content.measure(View.MeasureSpec.makeMeasureSpec(393,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(810,View.MeasureSpec.EXACTLY))
        content.layout(0,0,393,810)
        val bitmap=Bitmap.createBitmap(393,810,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.parseColor("#15263A"));content.draw(Canvas(bitmap))
        val folder=File("build/qa");folder.mkdirs();File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        assertTrue(bitmap.getPixel(120,300)!=bitmap.getPixel(1,1))
    }
    @Test fun renderHomeAndEveryGameWithNativeGraphics() {
        val prefs=RuntimeEnvironment.getApplication().getSharedPreferences("mindgame_v1",0)
        prefs.edit().clear().putBoolean("motion",false).commit()
        val controller=Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity=controller.get();capture(activity,"home")
        val launch=MainActivity::class.java.getDeclaredMethod("launch",String::class.java,Boolean::class.javaPrimitiveType);launch.isAccessible=true
        for(id in listOf("memory","mines","mastermind","2048","sudoku","nonogram")) {
            launch.invoke(activity,id,true)
            when(val g=activity.game) {
                is MemoryGame -> { for(v in 0..2) { val pair=g.cards.indices.filter { g.cards[it]==v };g.flip(pair[0]);g.flip(pair[1]) };activity.playPage(); assertTrue(activity.cells.any { it.background is com.ramy.mindgame.ui.RelicDrawable }) }
                is MinesGame -> { g.reveal(18); val i=g.mines.indices.first { g.open[it]==0 };g.flag(i);activity.playPage() }
                is NonogramGame -> {val owl=NonogramGame(pictureKey="owl");activity.newGame(owl);for(i in owl.board.indices)if(i%owl.size<owl.size*2/3)owl.mark(i,owl.solution[i]==0);activity.playPage()}
                is TilesGame -> {g.board=intArrayOf(2,4,8,2,4,16,32,8,128,256,64,16,2,4,0,0);g.points=1624;com.ramy.mindgame.core.LocalStore(activity).high("2048",12880);activity.playPage()}
                is CodeGame -> { g.submit(intArrayOf(0,1,2,3));g.submit(intArrayOf(3,2,1,0));activity.playPage() }
                is SudokuGame -> {val blanks=g.fixed.indices.filter{g.fixed[it]==0};blanks.take(5).forEach{i->(1..9).filter{!g.conflicts(i,it)}.take(3).forEach{g.note(i,it)}};activity.selected=blanks[3];g.enter(activity.selected,g.solution[activity.selected]);activity.playPage()}
                else -> Unit
            }
            capture(activity,id)
        }
        activity.newMines(16,16,40); capture(activity,"mines-intermediate");assertEquals(40,(activity.game as MinesGame).mineCount)
        activity.newMines(30,16,99);capture(activity,"mines-expert");assertEquals(30,(activity.game as MinesGame).columns)
        fun field(v: View): MinefieldView? { if(v is MinefieldView)return v;if(v is ViewGroup) for(i in 0 until v.childCount) {val found=field(v.getChildAt(i));if(found!=null)return found};return null }
        val minefield=field(activity.findViewById(android.R.id.content))!!
        minefield.onTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,220f,160f,0));minefield.onTouchEvent(MotionEvent.obtain(0,80,MotionEvent.ACTION_MOVE,80f,160f,0));minefield.onTouchEvent(MotionEvent.obtain(0,100,MotionEvent.ACTION_UP,80f,160f,0));assertFalse((activity.game as MinesGame).planted)
        minefield.zoomBy(.8f)
        minefield.onTouchEvent(MotionEvent.obtain(200,200,MotionEvent.ACTION_DOWN,110f,170f,0));minefield.onTouchEvent(MotionEvent.obtain(200,250,MotionEvent.ACTION_UP,110f,170f,0));assertTrue((activity.game as MinesGame).planted);assertFalse((activity.game as MinesGame).lost)
        launch.invoke(activity,"enigma",false)
        val intent=org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertEquals("com.codebreaker.game.EnigmaActivity",intent.component!!.className)
        val home=MainActivity::class.java.getDeclaredMethod("home");home.isAccessible=true;home.invoke(activity);capture(activity,"home-recent")
        controller.pause().stop().destroy()
    }
}
