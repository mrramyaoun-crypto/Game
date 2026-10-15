package com.ramy.mindgame.ui
import android.content.Context
import android.graphics.Typeface
import com.ramy.mindgame.R
/** Bundled faces keep game lettering consistent even on phones with a custom system font. */
object GameFonts {
 private var regular:Typeface?=null;private var bold:Typeface?=null
 @JvmStatic fun face(c:Context,heavy:Boolean=false):Typeface = if(heavy) bold ?: c.resources.getFont(R.font.antique_serif_bold).also{bold=it} else regular ?: c.resources.getFont(R.font.antique_serif).also{regular=it}
}
