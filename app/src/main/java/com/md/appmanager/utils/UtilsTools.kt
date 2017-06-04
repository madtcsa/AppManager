package com.md.appmanager.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils


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

        fun launchAppDetail(context: Context, appPkg: String) {
            try {
                if (TextUtils.isEmpty(appPkg)) return
                val uri = Uri.parse("market://details?id=" + appPkg)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.`package` = "com.android.vending"
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}