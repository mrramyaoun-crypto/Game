package com.ramy.mindgame
import android.app.Dialog
import android.graphics.*
import android.view.*
import android.widget.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.core.Registry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import org.robolectric.shadows.ShadowAlertDialog
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemePolishTests {
 private fun descendants(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{descendants(v.getChildAt(it))}else emptyList()
 private fun root(a:MainActivity)=a.findViewById<View>(android.R.id.content)
 private fun layout(a:MainActivity){val v=root(a);v.measure(View.MeasureSpec.makeMeasureSpec(393,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(810,View.MeasureSpec.EXACTLY));v.layout(0,0,393,810)}
 private fun button(a:MainActivity,s:String)=descendants(root(a)).first{it.contentDescription==s}
 private fun capture(a:MainActivity,d:Dialog,name:String){
  Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();layout(a);val v=d.window!!.decorView;v.measure(View.MeasureSpec.makeMeasureSpec(365,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(750,View.MeasureSpec.AT_MOST));v.layout(0,0,365,v.measuredHeight)
  val b=Bitmap.createBitmap(393,810,Bitmap.Config.ARGB_8888);val c=Canvas(b);root(a).draw(c);c.drawColor(0x99000000.toInt());c.save();c.translate(14f,(810-v.height)/2f);v.draw(c);c.restore();val f=File("build/qa/$name.png");f.parentFile!!.mkdirs();f.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}
  assertTrue(d.window!!.decorView.background is AntiquePanelDrawable)
  assertTrue(descendants(v).filterIsInstance<TextView>().any{it.currentTextColor==Theme.darkInk})
 }
 @Test fun everyPopupUsesParchmentAndNewGameCancelPreservesProgress(){
  RuntimeEnvironment.getApplication().getSharedPreferences("mindgame_v1",0).edit().clear().putBoolean("motion",false).commit()
  val ctrl=Robolectric.buildActivity(MainActivity::class.java).setup();val a=ctrl.get();a.newGame(CodeGame("mastermind"));layout(a)
  button(a,"Game menu and rules").performClick();var d=ShadowAlertDialog.getLatestAlertDialog();capture(a,d,"popup-menu");d.dismiss()
  a.restartGame();d=ShadowAlertDialog.getLatestAlertDialog();capture(a,d,"popup-new-game");val original=a.game;d.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick();assertSame(original,a.game)
  for(name in listOf("settings","statistics")){MainActivity::class.java.getDeclaredMethod("home").apply{isAccessible=true}.invoke(a);val m=MainActivity::class.java.getDeclaredMethod(name);m.isAccessible=true;m.invoke(a);d=ShadowAlertDialog.getLatestAlertDialog();capture(a,d,"popup-$name");assertTrue(descendants(d.window!!.decorView).filterIsInstance<TextView>().filter{it.text.isNotEmpty()&&it !is Button}.all{it.currentTextColor==Theme.darkInk});d.dismiss()}
  a.newMines(9,9,10);layout(a);button(a,"Choose minefield difficulty").performClick();d=ShadowAlertDialog.getLatestAlertDialog();capture(a,d,"popup-mines-difficulty");d.listView.performItemClick(null,3,3);d=ShadowAlertDialog.getLatestAlertDialog();capture(a,d,"popup-custom-mines");val fields=descendants(d.window!!.decorView).filterIsInstance<EditText>();assertEquals(3,fields.size);fields[0].setText("16");fields[1].setText("16");fields[2].setText("40");d.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();assertEquals(40,(a.game as MinesGame).mineCount);assertEquals(16,(a.game as MinesGame).columns)
  ctrl.pause().stop().destroy()
 }
 @Test fun commonNavigationStaysAtTopWhileMemoryScrollsAndLegendFits(){
  val ctrl=Robolectric.buildActivity(MainActivity::class.java).setup();val a=ctrl.get()
  for(g in listOf(CodeGame("mastermind"),MemoryGame(),NonogramGame(),SudokuGame(),MinesGame(),TilesGame())){a.newGame(g);layout(a);for(label in listOf("Explorer’s Desk","Game menu and rules")){val b=button(a,label) as GameControl;assertTrue(b.drawBody);assertEquals(5,b.top)}}
  a.newGame(MemoryGame(pairs=16));layout(a);val scroll=descendants(root(a)).filterIsInstance<ScrollView>().first();scroll.scrollTo(0,180);assertTrue(scroll.scrollY>0);val menu=button(a,"Game menu and rules");assertEquals(scroll.scrollY.toFloat(),menu.translationY);assertEquals(5f,menu.top+menu.translationY-scroll.scrollY)
  a.newGame(CodeGame("mastermind"));layout(a);val bmp=Bitmap.createBitmap(393,810,Bitmap.Config.ARGB_8888);root(a).draw(Canvas(bmp));val texts=descendants(root(a)).filterIsInstance<GameText>();val attempt=texts.first{it.contentDescription=="Attempt"};assertTrue(attempt.right<103);val legend=texts.first{it.contentDescription.toString().startsWith("Red =")};assertEquals(609,legend.top);assertFalse(texts.any{it.contentDescription.toString().contains("Tap a color")});assertTrue(Registry.get("mastermind").rules.contains("Four red pegs"))
  assertNotEquals(Typeface.create("serif",Typeface.NORMAL),GameFonts.face(a))
  ctrl.pause().stop().destroy()
 }
}
