package com.md.appmanager.activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

import com.afollestad.materialdialogs.MaterialDialog
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.md.appmanager.AppInfo
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.async.DeleteDataInBackground
import com.md.appmanager.async.ExtractFileInBackground
import com.md.appmanager.async.UninstallInBackground
import com.md.appmanager.utils.AppPreferences
import com.md.appmanager.utils.UtilsRoot
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog
import com.md.appmanager.utils.UtilsUI

class AppActivity : AppCompatActivity() {
    // Load Settings
    private var appPreferences: AppPreferences? = null

    // General variables
    private var appInfo: AppInfo? = null
    private var appsFavorite: MutableSet<String>? = null
    private var appsHidden: MutableSet<String>? = null

    // Configuration variables
    private val UNINSTALL_REQUEST_CODE = 1
    private var context: Context? = null
    private var activity: Activity? = null
    private var item_favorite: MenuItem? = null

    // UI variables
    private var fab: FloatingActionsMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        this.context = this
        this.activity = context as Activity?
        this.appPreferences = AppManagerApplication.appPreferences

        getInitialConfiguration()
        setInitialConfiguration()
        setScreenElements()

    }

    private fun setInitialConfiguration() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle("")
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        toolbar!!.setNavigationOnClickListener { onBackPressed() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            getWindow().setStatusBarColor(UtilsUI.darker(appPreferences!!.primaryColorPref, 0.8))
            toolbar.setBackgroundColor(appPreferences!!.primaryColorPref)
            if (appPreferences!!.navigationBlackPref!!) {
                getWindow().setNavigationBarColor(appPreferences!!.primaryColorPref)
            }
        }
    }

    private fun setScreenElements() {
        val header = findViewById(R.id.header) as TextView?
        val icon = findViewById(R.id.app_icon) as ImageView?
        val icon_googleplay = findViewById(R.id.app_googleplay) as ImageView?
        val name = findViewById(R.id.app_name) as TextView?
        val version = findViewById(R.id.app_version) as TextView?
        val apk = findViewById(R.id.app_apk) as TextView?
        val googleplay = findViewById(R.id.id_card) as CardView?
        val start = findViewById(R.id.start_card) as CardView?
        val extract = findViewById(R.id.extract_card) as CardView?
        val uninstall = findViewById(R.id.uninstall_card) as CardView?
        val cache = findViewById(R.id.cache_card) as CardView?
        val clearData = findViewById(R.id.clear_data_card) as CardView?
        fab = findViewById(R.id.fab) as FloatingActionsMenu?
        val fab_share = findViewById(R.id.fab_a) as FloatingActionButton?
        val fab_hide = findViewById(R.id.fab_b) as FloatingActionButton?
        val fab_buy = findViewById(R.id.fab_buy) as FloatingActionButton?

        icon!!.setImageDrawable(appInfo!!.icon)
        name!!.text = appInfo!!.name
        apk!!.text = appInfo!!.apk
        version!!.text = appInfo!!.version

        // Configure Colors
        header!!.setBackgroundColor(appPreferences!!.primaryColorPref)
        fab_share!!.colorNormal = appPreferences!!.fabColorPref
        fab_share.colorPressed = UtilsUI.darker(appPreferences!!.fabColorPref, 0.8)
        fab_hide!!.colorNormal = appPreferences!!.fabColorPref
        fab_hide.colorPressed = UtilsUI.darker(appPreferences!!.fabColorPref, 0.8)
        fab_buy!!.colorNormal = appPreferences!!.fabColorPref
        fab_buy.colorPressed = UtilsUI.darker(appPreferences!!.fabColorPref, 0.8)

        // CardView
        if (appInfo!!.isSystem!!) {
            icon_googleplay!!.visibility = View.GONE
            start!!.visibility = View.GONE
        } else {
            googleplay!!.setOnClickListener { UtilsApp.goToGooglePlay(context!!, appInfo!!.apk!!) }

            googleplay.setOnLongClickListener {
                val clipData: ClipData = ClipData.newPlainText("text", appInfo!!.apk)
                val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.primaryClip = clipData
                UtilsDialog.showSnackbar(activity!!, context!!.resources.getString(R.string.copied_clipboard), null, null, 2).show()
                false
            }

            start!!.setOnClickListener {
                try {
                    val intent = getPackageManager().getLaunchIntentForPackage(appInfo!!.apk)
                    startActivity(intent)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    UtilsDialog.showSnackbar(activity!!, String.format(resources.getString(R.string.dialog_cannot_open), appInfo!!.name), null, null, 2).show()
                }
            }

            uninstall!!.setOnClickListener {
                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                intent.data = Uri.parse("package:" + appInfo!!.apk)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                startActivityForResult(intent, UNINSTALL_REQUEST_CODE)
            }
        }
        extract!!.setOnClickListener {
            val dialog = UtilsDialog.showTitleContentWithProgress(context!!, String.format(resources.getString(R.string.dialog_saving), appInfo!!.name), resources.getString(R.string.dialog_saving_description))
            ExtractFileInBackground(context!!, dialog, appInfo!!).execute()
        }

        if (UtilsRoot.isRooted && AppManagerApplication.isPro()!!) {
            if (appInfo!!.isSystem!!) {
                uninstall!!.setOnClickListener {
                    val materialBuilder = UtilsDialog.showUninstall(context!!)
                            .callback(object : MaterialDialog.ButtonCallback() {
                                override fun onPositive(dialog: MaterialDialog?) {
                                    val dialogUninstalling = UtilsDialog.showTitleContentWithProgress(context!!, String.format(resources.getString(R.string.dialog_uninstalling), appInfo!!.name), resources.getString(R.string.dialog_uninstalling_description))
                                    UninstallInBackground(context!!, dialogUninstalling, appInfo!!).execute()
                                    dialog!!.dismiss()
                                }
                            })
                    materialBuilder.show()
                }
            }
            cache!!.visibility = View.VISIBLE
            cache.setOnClickListener {
                val dialog = UtilsDialog.showTitleContentWithProgress(context!!, resources.getString(R.string.dialog_cache_deleting), resources.getString(R.string.dialog_cache_deleting_description))
                DeleteDataInBackground(context!!, dialog, appInfo!!.data + "/cache/**", resources.getString(R.string.dialog_cache_success_description, appInfo!!.name)).execute()
            }
            clearData!!.visibility = View.VISIBLE
            clearData.setOnClickListener {
                val dialog = UtilsDialog.showTitleContentWithProgress(context!!, resources.getString(R.string.dialog_clear_data_deleting), resources.getString(R.string.dialog_clear_data_deleting_description))
                DeleteDataInBackground(context!!, dialog, appInfo!!.data + "/**", resources.getString(R.string.dialog_clear_data_success_description, appInfo!!.name)).execute()
            }
        } else if (appInfo!!.isSystem!!) {
            uninstall!!.visibility = View.GONE
            uninstall.foreground = null
        }

        // FAB (Share)
        fab_share.setOnClickListener {
            UtilsApp.copyFile(appInfo!!)
            val shareIntent = UtilsApp.getShareIntent(UtilsApp.getOutputFilename(appInfo!!))
            startActivity(Intent.createChooser(shareIntent, String.format(resources.getString(R.string.send_to), appInfo!!.name)))
        }

        // FAB (Hide)
        if (AppManagerApplication.isPro()!!) {
            fab_buy.visibility = View.GONE
            if (UtilsRoot.isRooted) {
                UtilsApp.setAppHidden(context!!, fab_hide, UtilsApp.isAppHidden(appInfo!!, appsHidden!!))
                fab_hide.visibility = View.VISIBLE
                fab_hide.setOnClickListener {
                    if (UtilsApp.isAppHidden(appInfo!!, appsHidden!!)!!) {
                        val hidden = UtilsRoot.hideWithRootPermission(appInfo!!.apk!!, true)
                        if (hidden) {
                            UtilsApp.removeIconFromCache(context!!, appInfo!!)
                            appsHidden!!.remove(appInfo!!.toString())
                            appPreferences!!.hiddenApps = appsHidden as MutableSet<String>
                            UtilsDialog.showSnackbar(activity!!, resources.getString(R.string.dialog_reboot), resources.getString(R.string.button_reboot), null, 3).show()
                        }
                    } else {
                        UtilsApp.saveIconToCache(context!!, appInfo!!)
                        val hidden = UtilsRoot.hideWithRootPermission(appInfo!!.apk!!, false)
                        if (hidden) {
                            appsHidden!!.add(appInfo!!.toString())
                            appPreferences!!.hiddenApps = appsHidden as MutableSet<String>
                        }
                    }
                    UtilsApp.setAppHidden(context!!, fab_hide, UtilsApp.isAppHidden(appInfo!!, appsHidden!!))
                }
            }
        } else {
            fab_buy.visibility = View.VISIBLE
            fab_buy.title = context!!.resources.getString(R.string.action_buy)
            fab_buy.setOnClickListener { UtilsDialog.showProFeatures(context!!) }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i("App", "OK")
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                finish()
                startActivity(intent)
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("App", "CANCEL")
            }
        }
    }

    private fun getInitialConfiguration() {
        val appName = intent.getStringExtra("app_name")
        val appApk = intent.getStringExtra("app_apk")
        val appVersion = intent.getStringExtra("app_version")
        val appSource = intent.getStringExtra("app_source")
        val appData = intent.getStringExtra("app_data")
        val bitmapByte = intent.getByteArrayExtra("app_icon")
        val bitmap = BitmapFactory.decodeByteArray(bitmapByte,0,bitmapByte.size)
        val appIcon = BitmapDrawable(resources, bitmap)
        val appIsSystem = intent.extras.getBoolean("app_isSystem")

        appInfo = AppInfo(appName, appApk, appVersion, appSource, appData, appIcon, appIsSystem)
        appsFavorite = appPreferences!!.favoriteApps
        appsHidden = appPreferences!!.hiddenApps

    }

    override fun onBackPressed() {
        if (fab!!.isExpanded) {
            fab!!.collapse()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        item_favorite = menu.findItem(R.id.action_favorite)
        UtilsApp.setAppFavorite(context!!, item_favorite!!, UtilsApp.isAppFavorite(appInfo!!.apk!!, appsFavorite!!))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                finish()
                return true
            }
            R.id.action_favorite -> {
                if (UtilsApp.isAppFavorite(appInfo!!.apk!!, appsFavorite!!)!!) {
                    appsFavorite!!.remove(appInfo!!.apk)
                    appPreferences!!.favoriteApps = appsFavorite as MutableSet<String>
                } else {
                    appsFavorite!!.add(appInfo!!.apk!!)
                    appPreferences!!.favoriteApps = appsFavorite as MutableSet<String>
                }
                UtilsApp.setAppFavorite(context!!, item_favorite!!, UtilsApp.isAppFavorite(appInfo!!.apk!!, appsFavorite!!))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
