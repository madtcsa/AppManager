package com.md.appmanager.async

import android.app.Activity
import android.content.Context
import android.os.AsyncTask

import com.afollestad.materialdialogs.MaterialDialog
import com.md.appmanager.R
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog
import com.md.appmanager.utils.UtilsRoot

class DeleteDataInBackground(private val context: Context, private val dialog: MaterialDialog, private val directory: String, private val successDescription: String) : AsyncTask<Void, String, Boolean>() {
    private val activity: Activity = context as Activity

    override fun doInBackground(vararg voids: Void): Boolean? {
        var status: Boolean? = false

        if (UtilsApp.checkPermissions(activity)!!) {
            status = UtilsRoot.removeWithRootPermission(directory)
        }

        return status
    }

    override fun onPostExecute(status: Boolean?) {
        super.onPostExecute(status)
        dialog.dismiss()
        if (status!!) {
            UtilsDialog.showSnackbar(activity, successDescription, null, null, 2).show()
        } else {
            UtilsDialog.showTitleContent(context, context.resources.getString(R.string.dialog_root_required), context.resources.getString(R.string.dialog_root_required_description))
        }
    }
}