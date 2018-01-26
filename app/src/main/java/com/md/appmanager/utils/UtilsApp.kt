package com.md.appmanager.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.MenuItem

import com.getbase.floatingactionbutton.FloatingActionButton
import com.md.appmanager.AppInfo
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object UtilsApp {
    private val MY_PERMISSIONS_REQUEST_WRITE_READ = 1

    /**
     * Default folder where APKs will be saved
     * @return File with the path
     */
    val defaultAppFolder: File
        get() = File(Environment.getExternalStorageDirectory().toString() + "/MLManager")

    /**
     * Custom folder where APKs will be saved
     * @return File with the path
     */
    val appFolder: File
        get() {
            val appPreferences = AppManagerApplication.appPreferences
            return File(appPreferences!!.customPath)
        }

    fun copyFile(appInfo: AppInfo): Boolean? {
        var res: Boolean? = false

        val initialFile = File(appInfo.source)
        val finalFile = getOutputFilename(appInfo)

        try {
            FileUtils.copyFile(initialFile, finalFile)
            res = true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return res
    }

    /**
     * Retrieve the name of the extracted APK
     * @param appInfo AppInfo
     * *
     * @return String with the output name
     */
    fun getAPKFilename(appInfo: AppInfo): String {
        val appPreferences = AppManagerApplication.appPreferences
        var res = ""
        if (appPreferences != null) {
            res = when (appPreferences.customFilename) {
                "1" -> appInfo.apk + "_" + appInfo.version
                "2" -> appInfo.name + "_" + appInfo.version
                "4" -> appInfo.name!!
                else -> appInfo.apk!!
            }
        }
        return res
    }

    /**
     * Retrieve the name of the extracted APK with the path
     * @param appInfo AppInfo
     * *
     * @return File with the path and output name
     */
    fun getOutputFilename(appInfo: AppInfo): File {
        return File(appFolder.path + "/" + getAPKFilename(appInfo) + ".apk")
    }

    /**
     * Delete all the extracted APKs
     * @return true if all files have been deleted, false otherwise
     */
    fun deleteAppFiles(): Boolean? {
        var res = false
        val f = appFolder
        if (f.exists() && f.isDirectory) {
            val files = f.listFiles()
            for (file in files) {
                file.delete()
            }
            if (f.listFiles().isEmpty()) {
                res = true
            }
        }
        return res
    }

    /**
     * Opens Google Play if installed, if not opens browser
     * @param context Context
     * *
     * @param id PackageName on Google Play
     */
    fun goToGooglePlay(context: Context, id: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + id)))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + id)))
        }
    }

    /**
     * Opens Google Plus
     * @param context Context
     * *
     * @param id Name on Google Play
     */
    fun goToGooglePlus(context: Context, id: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + id)))
    }

    /**
     * Retrieve your own app version
     * @param context Context
     * *
     * @return String with the app version
     */
    fun getAppVersionName(context: Context): String {
        var res = "0.0.0.0"
        try {
            res = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return res
    }

    /**
     * Retrieve your own app version code
     * @param context Context
     * *
     * @return int with the app version code
     */
    fun getAppVersionCode(context: Context): Int {
        var res = 0
        try {
            res = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return res
    }

    fun getShareIntent(file: File): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        intent.type = "application/vnd.android.package-archive"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return intent
    }

    /**
     * Retrieve if an app has been marked as favorite
     * @param apk App to check
     * *
     * @param appFavorites Set with apps
     * *
     * @return true if the app is marked as favorite, false otherwise
     */
    fun isAppFavorite(apk: String, appFavorites: Set<String>): Boolean? {
        var res: Boolean? = false
        if (appFavorites.contains(apk)) {
            res = true
        }

        return res
    }

    /**
     * Save the app as favorite
     * @param context Context
     * *
     * @param menuItem Item of the ActionBar
     * *
     * @param isFavorite true if the app is favorite, false otherwise
     */
    fun setAppFavorite(context: Context, menuItem: MenuItem, isFavorite: Boolean?) {
        if (isFavorite!!) {
            menuItem.icon = ContextCompat.getDrawable(context, R.drawable.ic_star_white)
        } else {
            menuItem.icon = ContextCompat.getDrawable(context, R.drawable.ic_star_border_white)
        }
    }

    /**
     * Retrieve if an app is hidden
     * @param appInfo App to check
     * *
     * @param appHidden Set with apps
     * *
     * @return true if the app is hidden, false otherwise
     */
    fun isAppHidden(appInfo: AppInfo, appHidden: Set<String>): Boolean? {
        var res: Boolean? = false
        if (appHidden.contains(appInfo.toString())) {
            res = true
        }

        return res
    }

    /**
     * Save the app as hidden
     * @param context Context
     * *
     * @param fabHide FAB button to change
     * *
     * @param isHidden true if the app is hidden, false otherwise
     */
    fun setAppHidden(context: Context, fabHide: FloatingActionButton, isHidden: Boolean?) {
        if (isHidden!!) {
            fabHide.title = context.resources.getString(R.string.action_unhide)
            fabHide.setIcon(R.drawable.ic_visibility_white)
        } else {
            fabHide.title = context.resources.getString(R.string.action_hide)
            fabHide.setIcon(R.drawable.ic_visibility_off_white)
        }
    }

    /**
     * Save an app icon to cache folder
     * @param context Context
     * *
     * @param appInfo App to save icon
     * *
     * @return true if the icon has been saved, false otherwise
     */
    fun saveIconToCache(context: Context, appInfo: AppInfo): Boolean? {
        var res: Boolean? = false

        try {
            val applicationInfo = context.packageManager.getApplicationInfo(appInfo.apk, 0)
            val fileUri = File(context.cacheDir, appInfo.apk)
            val out = FileOutputStream(fileUri)
            val icon = context.packageManager.getApplicationIcon(applicationInfo)
            val iconBitmap = icon as BitmapDrawable
            iconBitmap.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            res = true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return res
    }

    /**
     * Delelete an app icon from cache folder
     * @param context Context
     * *
     * @param appInfo App to remove icon
     * *
     * @return true if the icon has been removed, false otherwise
     */
    fun removeIconFromCache(context: Context, appInfo: AppInfo): Boolean {
        val file = File(context.cacheDir, appInfo.apk)
        return file.delete()
    }

    /**
     * Get an app icon from cache folder
     * @param context Context
     * *
     * @param appInfo App to get icon
     * *
     * @return Drawable with the app icon
     */
    fun getIconFromCache(context: Context, appInfo: AppInfo): Drawable {
        val res: Drawable
        res = try {
            val fileUri = File(context.cacheDir, appInfo.apk)
            val bitmap = BitmapFactory.decodeFile(fileUri.path)
            BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            context.resources.getDrawable(R.drawable.ic_android)
        }
        return res
    }

    fun extractMLManagerPro(context: Context, appInfo: AppInfo): Boolean? {
        var res: Boolean = false
        val finalFile = File(appFolder.path, getAPKFilename(appInfo) + ".png")

        try {
            val fileUri = File(context.cacheDir, getAPKFilename(appInfo) + ".png")
            val out = FileOutputStream(fileUri)
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.banner_troll)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            FileUtils.moveFile(fileUri, finalFile)
            res = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return res
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions(activity: Activity): Boolean {
        var res = false
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_READ)
        } else {
            res = true
        }
        return res
    }
}
