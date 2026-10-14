package com.ramy.mindgame
import android.graphics.*
import android.view.*
import android.widget.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.ui.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import java.io.File
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PhysicalGameUiTests{
 private fun descendants(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{descendants(v.getChildAt(it))}else emptyList()
 private fun root(a:MainActivity)=a.findViewById<View>(android.R.id.content)
 private fun layout(a:MainActivity,w:Int=393,h:Int=810){val v=root(a);v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));v.layout(0,0,w,h)}
 private fun button(a:MainActivity,s:String)=descendants(root(a)).first{it.contentDescription==s}
 private fun tap(v:View,x:Float,y:Float){v.dispatchTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0));v.dispatchTouchEvent(MotionEvent.obtain(0,30,MotionEvent.ACTION_UP,x,y,0))}
 private fun app():org.robolectric.android.controller.ActivityController<MainActivity>{RuntimeEnvironment.getApplication().getSharedPreferences("mindgame_v1",0).edit().clear().putBoolean("motion",false).commit();return Robolectric.buildActivity(MainActivity::class.java).setup()}
 @Test fun nativePaletteCardsSudokuAndSwipeChangeRealSavedGames(){
  val ctrl=app();val a=ctrl.get();val code=CodeGame("mastermind",kotlin.random.Random(5));a.newGame(code);layout(a)
  for(s in listOf("red","blue","green","gold"))button(a,"Choose $s").performClick();assertArrayEquals(intArrayOf(0,3,2,1),a.codeInput);button(a,"Check guess").performClick();assertArrayEquals(intArrayOf(0,3,2,1),code.attempts.single())
  val memory=MemoryGame(kotlin.random.Random(2));a.newGame(memory);layout(a);val pair=memory.cards.indices.filter{memory.cards[it]==0};a.cells[pair[0]].performClick();assertEquals(pair[0],memory.first);a.cells[pair[1]].performClick();assertEquals(2,memory.matched.sum());assertTrue(a.cells[pair[0]].background is RelicDrawable)
  val sudoku=SudokuGame(kotlin.random.Random(3));a.newGame(sudoku);layout(a);val grid=descendants(root(a)).filterIsInstance<SudokuBoardView>().single();val i=sudoku.fixed.indices.first{sudoku.fixed[it]==0};tap(grid,(i%9+.5f)*grid.width/9,(i/9+.5f)*grid.height/9);assertEquals(i,a.selected);button(a,"Notes").performClick();button(a,"Enter 3").performClick();assertTrue(sudoku.notes[i] and(1 shl 3)!=0);assertEquals(0,sudoku.board[i]);button(a,"Undo").performClick();assertEquals(0,sudoku.notes[i]);button(a,"Notes").performClick();button(a,"Enter ${sudoku.solution[i]}").performClick();assertEquals(sudoku.solution[i],sudoku.board[i]);button(a,"Erase").performClick();assertEquals(0,sudoku.board[i])
  val tiles=TilesGame(kotlin.random.Random(3));tiles.board=intArrayOf(2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0);a.newGame(tiles);layout(a);val board=descendants(root(a)).filterIsInstance<TilesBoardView>().single();val before=tiles.board.copyOf();board.dispatchTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,290f,100f,0));board.dispatchTouchEvent(MotionEvent.obtain(0,50,MotionEvent.ACTION_UP,40f,100f,0));assertEquals(4,tiles.points);assertEquals(4,tiles.board[0]);button(a,"Undo").performClick();assertArrayEquals(before,tiles.board)
  ctrl.pause().stop().destroy()
 }
 @Test fun difficultyPanFlagAndMosaicStrokeRemainFunctional(){
  val ctrl=app();val a=ctrl.get();a.newMines(9,9,10);layout(a);button(a,"Choose minefield difficulty").performClick();val d=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();val list=d.listView;list.performItemClick(list.getChildAt(1),1,1);layout(a);val mines=a.game as MinesGame;assertEquals(16,mines.columns);assertEquals(40,mines.mineCount);val field=descendants(root(a)).filterIsInstance<MinefieldView>().single();button(a,"Flag").performClick();tap(field,20f,20f);assertEquals(1,mines.flags[0]);assertFalse(mines.planted);button(a,"Reveal").performClick();tap(field,field.width/8f*1.5f,field.width/8f*1.5f);assertTrue(mines.planted);assertFalse(mines.lost)
  val mosaic=NonogramGame(pictureKey="owl");a.newGame(mosaic);layout(a);val view=descendants(root(a)).filterIsInstance<MosaicView>().single();val v:(String)->Float={n->MosaicView::class.java.getDeclaredField(n).apply{isAccessible=true}.getFloat(view)};val x=v("left");val y=v("top");val tile=v("tile");view.dispatchTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x+tile*.5f,y+tile*.5f,0));view.dispatchTouchEvent(MotionEvent.obtain(0,50,MotionEvent.ACTION_MOVE,x+tile*3.5f,y+tile*.5f,0));view.dispatchTouchEvent(MotionEvent.obtain(0,70,MotionEvent.ACTION_UP,x+tile*3.5f,y+tile*.5f,0));assertEquals(4,mosaic.board.count{it==1});assertEquals(1,mosaic.moves);button(a,"Undo").performClick();assertEquals(0,mosaic.board.sum());button(a,"Mark ×").performClick();tap(view,x+tile*.5f,y+tile*.5f);assertEquals(2,mosaic.board[0]);button(a,"Pan").performClick();val before=mosaic.board.copyOf();tap(view,x+tile*1.5f,y+tile*.5f);assertArrayEquals(before,mosaic.board)
  ctrl.pause().stop().destroy()
 }
 @Test fun sixBoardsFitPhoneWidthsAndLargeMemoryHuntsScroll(){
  val ctrl=app();val a=ctrl.get();val games=listOf(CodeGame("mastermind"),MemoryGame(),SudokuGame(),MinesGame(),NonogramGame(),TilesGame())
  for(w in listOf(360,393,412)){for(g in games){a.newGame(g);layout(a,w,if(w==360)640 else 810);val table=a.gameTable!!;assertEquals(w,table.width);for(v in descendants(table).filterIsInstance<GameControl>()){assertTrue("${g.id}: ${v.label}",v.left>=0&&v.right<=w&&v.top>=0&&v.bottom<=table.height)};val b=Bitmap.createBitmap(w,table.height,Bitmap.Config.ARGB_8888);table.draw(Canvas(b));val f=File("build/qa/phone-$w-${g.id}.png");f.parentFile!!.mkdirs();f.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}}
  a.newGame(MemoryGame(pairs=16));layout(a,360,640);assertTrue(a.gameTable!!.height>640);assertEquals(32,a.cells.size)
  ctrl.pause().stop().destroy()
 }
}
