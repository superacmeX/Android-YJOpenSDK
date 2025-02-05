package com.superacme.biz_bind.core

import androidx.compose.ui.unit.Dp


val TAG = "CommonExtension"

fun scaleDp(dp: Float): Float {
//    val density = Resources.getSystem().displayMetrics.density
//    val width = Resources.getSystem().displayMetrics.widthPixels
//    val height = Resources.getSystem().displayMetrics.heightPixels
//    val screenWidth = if(width<=height)width else height
//    val realPixel = (dp / designScreenWidth) * screenWidth
//    return (realPixel / density).toFloat()
    return dp / 2
}

val Int.sdp: Dp get() = Dp(value = scaleDp(this.toFloat()))

fun String.totalCalculateLength(): Int {
    return this.toCharArray().sumOf {
        if (isChinese(it)) 2.toInt() else 1.toInt()
    }
}

fun isChinese(c: Char): Boolean {
    val ub = Character.UnicodeBlock.of(c)
    return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION
}
