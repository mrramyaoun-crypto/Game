package com.ramy.mindgame.ui
import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import com.ramy.mindgame.core.Registry

/** One compact guide per game; scrolling only accommodates accessibility font sizes. */
object RuleGuide {
    @JvmStatic fun show(activity: Activity,id: String) {
        val info=Registry.get(id);val dialog=Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val page=Theme.column(activity);page.background=Theme.paper(activity);val pad=Theme.dp(activity,18);page.setPadding(pad,pad,pad,pad)
        page.addView(Theme.text(activity,info.name,24f,Theme.darkInk,true))
        val scroll=ScrollView(activity);val text=Theme.column(activity)
        info.rules.split("\n\n").forEach { section -> val pair=section.split("\n",limit=2);text.addView(Theme.text(activity,pair[0],11f,0xFF286C63.toInt(),true));text.addView(Theme.text(activity,pair.getOrElse(1){""},14f,Theme.darkInk).apply {setLineSpacing(Theme.dp(activity,2).toFloat(),1f)});text.addView(Theme.space(activity,4)) }
        scroll.addView(text);page.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));page.addView(Theme.button(activity,"Got it · let’s play") {dialog.dismiss()})
        dialog.setContentView(page);dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT));dialog.show()
        val dm=activity.resources.displayMetrics;dialog.window?.setLayout(dm.widthPixels-Theme.dp(activity,24),(dm.heightPixels*.89f).toInt())
    }
}
