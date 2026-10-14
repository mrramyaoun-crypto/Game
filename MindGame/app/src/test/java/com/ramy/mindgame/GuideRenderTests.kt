package com.ramy.mindgame
import android.view.*
import android.widget.*
import android.graphics.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import com.ramy.mindgame.ui.*
import com.ramy.mindgame.games.*
import com.ramy.mindgame.core.Registry
import java.io.File
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GuideRenderTests {
    private fun descendants(v: View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{descendants(v.getChildAt(it))}else emptyList()
    @Test fun launcherUsesAdaptiveOwl(){val app=RuntimeEnvironment.getApplication();val icon=app.getDrawable(com.ramy.mindgame.R.mipmap.ic_launcher)!!;assertTrue(icon is android.graphics.drawable.AdaptiveIconDrawable);icon.setBounds(0,0,192,192);val b=Bitmap.createBitmap(192,192,Bitmap.Config.ARGB_8888);icon.draw(Canvas(b));val f=File("build/qa/owl-icon.png");f.parentFile!!.mkdirs();f.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)}}
    @Test fun guidesFitOnePhoneScreenAndColorPaletteSelectsDirectly(){
        RuntimeEnvironment.getApplication().getSharedPreferences("mindgame_v1",0).edit().clear().putBoolean("motion",false).commit()
        val control=Robolectric.buildActivity(MainActivity::class.java).setup();val a=control.get()
        Registry.games.forEach{info->RuleGuide.show(a,info.id);val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val v=dialog.findViewById<View>(android.R.id.content);v.measure(View.MeasureSpec.makeMeasureSpec(369,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(751,View.MeasureSpec.EXACTLY));v.layout(0,0,369,751);val scroll=descendants(v).filterIsInstance<ScrollView>().first();assertTrue("${info.name}: guide scrolls at standard font",scroll.getChildAt(0).height<=scroll.height);val bitmap=Bitmap.createBitmap(369,751,Bitmap.Config.ARGB_8888);v.draw(Canvas(bitmap));val f=File("build/qa/rules-${info.id}.png");f.parentFile!!.mkdirs();f.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};dialog.dismiss()}
        a.newGame(CodeGame("mastermind",kotlin.random.Random(4)));val root=a.findViewById<View>(android.R.id.content);val palette=descendants(root).first{it.contentDescription=="Choose blue"};palette.performClick();assertEquals(3,a.codeInput[0]);descendants(root).first{it.contentDescription?.startsWith("Position 1,")==true}.performClick();descendants(root).first{it.contentDescription=="Choose orange"}.performClick();assertEquals(5,a.codeInput[0]);assertEquals(-1,a.codeInput[1]);control.pause().stop().destroy()
    }
}
