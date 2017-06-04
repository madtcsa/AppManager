package com.md.appmanager.utils

import android.content.res.Resources
import android.util.TypedValue

/**
 * Created by chenwei on 2017/6/2.
 */
class UtilsTools {


    /**
     * Convert Dp to Pixel
     */

    companion object Tools {
        fun dpToPx(dp: Float, resources: Resources): Int {
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            return px.toInt()
        }
    }
}