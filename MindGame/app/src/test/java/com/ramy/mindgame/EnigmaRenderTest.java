package com.ramy.mindgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,qualifiers="w393dp-h851dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EnigmaRenderTest {
    private View findText(View view,String text) {
        if(view instanceof android.widget.TextView && ((android.widget.TextView)view).getText().toString().equals(text))return view;
        if(view instanceof android.view.ViewGroup) {android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++) {View found=findText(group.getChildAt(i),text);if(found!=null)return found;}}return null;
    }
    @Test public void originalScreenRendersAndPreservesScratchCandidates() throws Exception {
        // Restore a deterministic state to exercise the actual v13 view hierarchy.
        JSONObject j=new JSONObject().put("secret","1234").put("mode","normal").put("active",0).put("attempted",false).put("assist",false).put("solved",false).put("left",3);
        JSONArray clues=new JSONArray();
        clues.put(new JSONObject().put("code","1678").put("total",1).put("right",1));
        clues.put(new JSONObject().put("code","5267").put("total",1).put("right",1));
        clues.put(new JSONObject().put("code","6839").put("total",1).put("right",1));
        clues.put(new JSONObject().put("code","9784").put("total",1).put("right",1));
        clues.put(new JSONObject().put("code","3516").put("total",2).put("right",0));
        j.put("clues",clues).put("slots",new JSONArray("[49,50,0,0]")).put("eliminated",new JSONArray("[54,55,56]")).put("candidates",new JSONArray("[\"1234\",\"1934\",\"1239\"]")).put("crossed",new JSONArray("[1]"));
        Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("mindgame_v1",0).edit().putString("legacy_enigma_v13",j.toString()).commit();
        Class<? extends Activity> clazz=(Class<? extends Activity>)Class.forName("com.codebreaker.game.EnigmaActivity");
        org.robolectric.android.controller.ActivityController<? extends Activity> controller=Robolectric.buildActivity(clazz).setup();
        Activity activity=controller.get();View content=activity.findViewById(android.R.id.content);
        content.measure(View.MeasureSpec.makeMeasureSpec(393,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(810,View.MeasureSpec.EXACTLY));content.layout(0,0,393,810);
        Bitmap bitmap=Bitmap.createBitmap(393,810,Bitmap.Config.ARGB_8888);content.draw(new Canvas(bitmap));File folder=new File("build/qa");folder.mkdirs();try(FileOutputStream f=new FileOutputStream(new File(folder,"enigma.png"))) { bitmap.compress(Bitmap.CompressFormat.PNG,100,f); }
        View candidate=findText(content,"1234,");assertNotNull(candidate);android.graphics.Rect visible=new android.graphics.Rect();assertTrue("Candidate scratch-outs must remain visible",candidate.getLocalVisibleRect(visible));assertTrue(visible.height()>=32);candidate.performClick();
        View key=findText(content,"9");assertNotNull(key);key.performClick();
        controller.pause().stop().destroy();
        JSONObject restored=new JSONObject(app.getSharedPreferences("mindgame_v1",0).getString("legacy_enigma_v13","{}"));
        assertEquals(3,restored.getJSONArray("candidates").length());assertEquals(2,restored.getJSONArray("crossed").length());assertEquals((int)'9',restored.getJSONArray("slots").getInt(0));assertEquals("1234",restored.getString("secret"));
    }
}
