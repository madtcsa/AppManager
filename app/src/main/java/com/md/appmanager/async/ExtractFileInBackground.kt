package com.md.appmanager.async

import android.app.Activity
import android.content.Context
import android.os.AsyncTask

import com.afollestad.materialdialogs.MaterialDialog
import com.md.appmanager.AppInfo
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog

class ExtractFileInBackground(private val context: Context, private val dialog: MaterialDialog, private val appInfo: AppInfo) : AsyncTask<Void, String, Boolean>() {
    private val activity: Activity = context as Activity

    override fun doInBackground(vararg voids: Void): Boolean? {
        var status: Boolean? = false

        if (UtilsApp.checkPermissions(activity)!!) {
            if (appInfo.apk != AppManagerApplication.proPackage) {
                status = UtilsApp.copyFile(appInfo)
            } else {
                status = UtilsApp.extractMLManagerPro(context, appInfo)
            }
        }

        return status
    }

    override fun onPostExecute(status: Boolean?) {
        super.onPostExecute(status)
        dialog.dismiss()
        if (status!!) {
            UtilsDialog.showSnackbar(activity, String.format(context.resources.getString(R.string.dialog_saved_description), appInfo.name, UtilsApp.getAPKFilename(appInfo)), context.resources.getString(R.string.button_undo), UtilsApp.getOutputFilename(appInfo), 1).show()
        } else {
            UtilsDialog.showTitleContent(context, context.resources.getString(R.string.dialog_extract_fail), context.resources.getString(R.string.dialog_extract_fail_description))
        }
    }
}