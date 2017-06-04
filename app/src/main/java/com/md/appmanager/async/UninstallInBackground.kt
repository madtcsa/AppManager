package com.md.appmanager.async

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask

import com.afollestad.materialdialogs.MaterialDialog
import com.md.appmanager.AppInfo
import com.md.appmanager.R
import com.md.appmanager.activities.MainActivity
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog
import com.md.appmanager.utils.UtilsRoot

class UninstallInBackground(private val context: Context, private val dialog: MaterialDialog, private val appInfo: AppInfo) : AsyncTask<Void, String, Boolean>() {
    private val activity: Activity = context as Activity

    override fun doInBackground(vararg voids: Void): Boolean? {
        var status: Boolean? = false

        if (UtilsApp.checkPermissions(activity)!!) {
            status = UtilsRoot.uninstallWithRootPermission(appInfo.source!!)
        }

        return status
    }

    override fun onPostExecute(status: Boolean?) {
        super.onPostExecute(status)
        dialog.dismiss()
        if (status!!) {
            val materialDialog = UtilsDialog.showUninstalled(context, appInfo)
            materialDialog.callback(object : MaterialDialog.ButtonCallback() {
                override fun onPositive(dialog: MaterialDialog?) {
                    UtilsRoot.rebootSystem()
                    dialog!!.dismiss()
                }
            })
            materialDialog.callback(object : MaterialDialog.ButtonCallback() {
                override fun onNegative(dialog: MaterialDialog?) {
                    dialog!!.dismiss()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.finish()
                    context.startActivity(intent)
                }
            })
            materialDialog.show()
        } else {
            UtilsDialog.showTitleContent(context, context.resources.getString(R.string.dialog_root_required), context.resources.getString(R.string.dialog_root_required_description))
        }
    }
}