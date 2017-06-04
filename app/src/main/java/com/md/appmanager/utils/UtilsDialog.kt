package com.md.appmanager.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import com.md.appmanager.AppInfo
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.view.material.CustomSnackBar
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable

import java.io.File

object UtilsDialog {

    fun showTitleContent(context: Context, title: String, content: String): MaterialDialog {
        val materialBuilder = MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(context.resources.getString(android.R.string.ok))
                .cancelable(true)
        return materialBuilder.show()
    }

    fun showTitleContentWithProgress(context: Context, title: String, content: String): MaterialDialog {
        val materialBuilder = MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .cancelable(false)
                .progress(true, 0)
        return materialBuilder.show()
    }

    fun showUninstall(context: Context): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(context.resources.getString(R.string.dialog_uninstall_root))
                .content(context.resources.getString(R.string.dialog_uninstall_root_description))
                .positiveText(context.resources.getString(R.string.button_uninstall))
                .negativeText(context.resources.getString(android.R.string.cancel))
                .cancelable(false)
    }

    fun showUninstalled(context: Context, appInfo: AppInfo): MaterialDialog.Builder {
        return MaterialDialog.Builder(context)
                .title(String.format(context.resources.getString(R.string.dialog_uninstalled_root), appInfo.name))
                .content(context.resources.getString(R.string.dialog_uninstalled_root_description))
                .positiveText(context.resources.getString(R.string.button_reboot))
                .negativeText(context.resources.getString(R.string.button_later))
                .cancelable(false)
    }

    /**
     * Show Snackbar
     * @param activity Activity
     * *
     * @param text Text of the Snackbar
     * *
     * @param buttonText Button text of the Snackbar
     * *
     * @param file File to remove if style == 1
     * *
     * @param style 1 for extracted APKs, 2 display without button and 3 for hidden apps
     * *
     * @return Snackbar to show
     */
    fun showSnackbar(activity: Activity, text: String, buttonText: String?, file: File?, style: Int?): CustomSnackBar {
        val snackBar: CustomSnackBar

        when (style) {
            1 -> snackBar = CustomSnackBar(activity, text, buttonText, View.OnClickListener { file!!.delete() })
            2 -> snackBar = CustomSnackBar(activity, text, null, null)
            3 -> snackBar = CustomSnackBar(activity, text, buttonText, View.OnClickListener { UtilsRoot.rebootSystem() })
            else -> snackBar = CustomSnackBar(activity, text, null, null)
        }

        return snackBar
    }

    fun showProFeatures(context: Context): MaterialDialog {
        val adapter = MaterialSimpleListAdapter(context)
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(context.resources.getString(R.string.pro_feature_1))
                .icon(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_visibility_off).color(Color.GRAY).sizeDp(18))
                .backgroundColor(Color.WHITE)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(context.resources.getString(R.string.pro_feature_2))
                .icon(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_list).color(Color.GRAY).sizeDp(18))
                .backgroundColor(Color.WHITE)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(context.resources.getString(R.string.pro_feature_3))
                .icon(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_phonelink_erase).color(Color.GRAY).sizeDp(18))
                .backgroundColor(Color.WHITE)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(context)
                .content(context.resources.getString(R.string.pro_feature_4))
                .icon(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_delete).color(Color.GRAY).sizeDp(18))
                .backgroundColor(Color.WHITE)
                .build())

        val materialBuilder = MaterialDialog.Builder(context)
                .title(context.resources.getString(R.string.action_buy) + " (" + context.resources.getString(R.string.action_buy_description) + ")")
                .icon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                .adapter(adapter) { materialDialog, view, i, charSequence -> }
                .positiveText(context.resources.getString(R.string.action_buy) + " ($1.43)")
                .negativeText(context.resources.getString(R.string.button_later))
                .onPositive { dialog, which -> UtilsApp.goToGooglePlay(context, AppManagerApplication.proPackage) }

        return materialBuilder.show()
    }

}
