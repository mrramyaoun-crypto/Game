package com.ramy.mindgame

import android.graphics.*
import android.view.*
import android.widget.TextView
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.games.NonogramGame
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExplorerDeskTests {
    private fun frame(v: View,w: Int,h: Int){v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));v.layout(0,0,w,h)}
    private fun render(v:View,name:String,w:Int=393,h:Int=810){frame(v,w,h);val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);v.draw(Canvas(bitmap));val folder=File("build/qa");folder.mkdirs();File(folder,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
    private fun children(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{children(v.getChildAt(it))}else emptyList()
    @Test fun allObjectsHaveIndependentNativeTargetsAcrossPhoneSizes(){
        val app=RuntimeEnvironment.getApplication();val taps=mutableListOf<String>();val resume=DeskResume("nonogram","Mosaic Restoration","The Hidden Owl","3:42",.4f,true,"Resume")
        val desk=ExplorerDesk(app,{false},resume,{taps.add(it)},{taps.add("settings")},{taps.add("library")},{taps.add("collection")},{taps.add("stats")},{})
        for((w,h)in listOf(393 to 810,360 to 640,412 to 860)){
            frame(desk,w,h)
            for(id in listOf("enigma","mastermind","memory","nonogram","sudoku","mines","2048","settings","resume","library","collection","stats")){
                val bounds=desk.targetBounds(id);assertTrue("$id bounds",bounds.width()>=48&&bounds.height()>=48);assertTrue(bounds.left>=0&&bounds.top>=0&&bounds.right<=w&&bounds.bottom<=h)
                val x=bounds.exactCenterX();val y=bounds.exactCenterY();desk.dispatchTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0));desk.dispatchTouchEvent(MotionEvent.obtain(0,80,MotionEvent.ACTION_UP,x,y,0));assertEquals(if(id=="resume")"nonogram"else id,taps.last())
            }
        }
        assertEquals(36,taps.size);assertFalse(children(desk).any{it is TextView});render(desk,"home-desk-360",360,640);render(desk,"home-desk-412",412,860)
        val target=children(desk).first{it.tag=="desk:enigma"};target.dispatchTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,10f,10f,0));target.dispatchTouchEvent(MotionEvent.obtain(0,5,MotionEvent.ACTION_CANCEL,10f,10f,0));assertEquals(36,taps.size)
    }
    @Test fun actualActivityRendersReferenceResumeAndFreshDesk(){
        val prefs=RuntimeEnvironment.getApplication().getSharedPreferences("mindgame_v1",0);prefs.edit().clear().putBoolean("motion",false).commit()
        val control=Robolectric.buildActivity(MainActivity::class.java).setup();val a=control.get();render(a.findViewById(android.R.id.content),"home-desk-fresh")
        val puzzle=NonogramGame(pictureKey="owl");for(i in puzzle.board.indices)if(i%puzzle.size<6)puzzle.mark(i,puzzle.solution[i]==0)
        prefs.edit().putString("recent","nonogram").putString("save_nonogram",puzzle.save().put("elapsed",222000).put("recorded",false).toString()).commit()
        val home=MainActivity::class.java.getDeclaredMethod("home");home.isAccessible=true;home.invoke(a);val root=a.findViewById<View>(android.R.id.content);render(root,"home-desk-approved-state")
        val mosaic=children(root).first{it.tag=="desk:nonogram"};mosaic.performClick();assertEquals("nonogram",a.game!!.id);home.invoke(a)
        children(a.findViewById(android.R.id.content)).first{it.tag=="desk:enigma"}.performClick();val intent=org.robolectric.Shadows.shadowOf(a).nextStartedActivity;assertEquals("com.codebreaker.game.EnigmaActivity",intent.component!!.className)
        control.pause().resume();assertTrue(children(a.findViewById(android.R.id.content)).first{it.tag=="desk:resume"}.contentDescription.toString().contains("Enigma"))
        control.pause().stop().destroy()
    }
}
