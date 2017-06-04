package com.md.appmanager.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout

import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.utils.AppPreferences
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsUI

import net.rdrei.android.dirchooser.DirectoryChooserConfig
import net.rdrei.android.dirchooser.DirectoryChooserFragment

import yuku.ambilwarna.widget.AmbilWarnaPreference
import java.util.concurrent.TimeUnit

class SettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
    // Load Settings
    private var appPreferences: AppPreferences? = null
    private var toolbar: Toolbar? = null
    private var context: Context? = null

    private var prefVersion: Preference? = null
    private var prefLicense: Preference? = null
    private var prefDeleteAll: Preference? = null
    private var prefNavigationBlack: Preference? = null
    private var prefCustomPath: Preference? = null
    private var prefCustomFilename: ListPreference? = null
    private var prefSortMode: ListPreference? = null
    private var chooserDialog: DirectoryChooserFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        this.context = this
        this.appPreferences = AppManagerApplication.appPreferences

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        prefVersion = findPreference("prefVersion")
        prefLicense = findPreference("prefLicense")
        prefDeleteAll = findPreference("prefDeleteAll")
        prefCustomFilename = findPreference("prefCustomFilename") as ListPreference
        prefSortMode = findPreference("prefSortMode") as ListPreference
        prefCustomPath = findPreference("prefCustomPath")

        setInitialConfiguration()

        val versionName = UtilsApp.getAppVersionName(context as SettingsActivity)
        val versionCode = UtilsApp.getAppVersionCode(context as SettingsActivity)

        prefVersion!!.title = resources.getString(R.string.app_name) + " v" + versionName + " (" + versionCode + ")"
        prefVersion!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back)
            false
        }

        prefLicense!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(context, LicenseActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back)
            false
        }

        // prefCustomFilename
        setCustomFilenameSummary()

        // prefSortMode
        setSortModeSummary()

        // prefCustomPath
        setCustomPathSummary()

        // prefDeleteAll
        prefDeleteAll!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            prefDeleteAll!!.setSummary(R.string.deleting)
            prefDeleteAll!!.isEnabled = false
            val deleteAll = UtilsApp.deleteAppFiles()
            if (deleteAll!!) {
                prefDeleteAll!!.setSummary(R.string.deleting_done)
            } else {
                prefDeleteAll!!.setSummary(R.string.deleting_error)
            }
            prefDeleteAll!!.isEnabled = true
            true
        }

        // prefCustomPath
        prefCustomPath!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val chooserConfig = DirectoryChooserConfig.builder()
                    .newDirectoryName("ML Manager APKs")
                    .allowReadOnlyDirectory(false)
                    .allowNewDirectoryNameModification(true)
                    .initialDirectory(appPreferences!!.customPath)
                    .build()

            chooserDialog = DirectoryChooserFragment.newInstance(chooserConfig)
            chooserDialog!!.show(fragmentManager, null)

            false
        }

    }

    override fun setContentView(layoutResID: Int) {
        val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
        toolbar = contentView.findViewById(R.id.toolbar) as Toolbar
        toolbar!!.setTitleTextColor(resources.getColor(R.color.white))
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
        toolbar!!.navigationIcon = UtilsUI.tintDrawable(toolbar!!.navigationIcon!!, ColorStateList.valueOf(getColor(R.color.white)))

        val contentWrapper = contentView.findViewById(R.id.content_wrapper) as ViewGroup
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)
        window.setContentView(contentView)

    }

    private fun setInitialConfiguration() {
        toolbar!!.title = resources.getString(R.string.action_settings)

        // Android 5.0+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = UtilsUI.darker(appPreferences!!.primaryColorPref, 0.8)
            toolbar!!.setBackgroundColor(appPreferences!!.primaryColorPref)
            if (appPreferences!!.navigationBlackPref!!) {
                window.navigationBarColor = appPreferences!!.primaryColorPref
            }
        }

        // Pre-Lollipop devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            prefNavigationBlack!!.isEnabled = false
            prefNavigationBlack!!.setDefaultValue(true)
        }
    }

    private fun setCustomFilenameSummary() {
        val filenameValue = Integer.valueOf(appPreferences!!.customFilename)!! - 1
        prefCustomFilename!!.summary = resources.getStringArray(R.array.filenameEntries)[filenameValue]
    }

    private fun setSortModeSummary() {
        val sortValue = Integer.valueOf(appPreferences!!.sortMode)!! - 1
        prefSortMode!!.summary = resources.getStringArray(R.array.sortEntries)[sortValue]
    }

    private fun setCustomPathSummary() {
        val path = appPreferences!!.customPath
        if (path == UtilsApp.defaultAppFolder.getPath()) {
            prefCustomPath!!.summary = resources.getString(R.string.button_default) + ": " + UtilsApp.defaultAppFolder.getPath()
        } else {
            prefCustomPath!!.summary = path
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = findPreference(key)

        if (pref === prefCustomFilename) {
            setCustomFilenameSummary()
        } else if (pref === prefSortMode) {
            setSortModeSummary()
        } else if (pref === prefCustomPath) {
            setCustomPathSummary()
        }
    }

    override fun onSelectDirectory(path: String) {
        appPreferences!!.customPath = path
        setCustomPathSummary()
        chooserDialog!!.dismiss()
    }

    override fun onCancelChooser() {
        chooserDialog!!.dismiss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right)
    }

}
