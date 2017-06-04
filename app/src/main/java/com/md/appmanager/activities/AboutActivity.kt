package com.md.appmanager.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import android.widget.TextView
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.utils.AppPreferences
import com.md.appmanager.utils.UtilsUI

class AboutActivity : AppCompatActivity() {
    // Load Settings
    internal var appPreferences: AppPreferences? = null

    // About variables
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        this.appPreferences = AppManagerApplication.appPreferences
        this.context = this
        setInitialConfiguration()
        setScreenElements()
    }

    private fun setInitialConfiguration() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.action_about)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = UtilsUI.darker(appPreferences!!.primaryColorPref, 0.8)
            toolbar.setBackgroundColor(appPreferences!!.primaryColorPref)
            if (appPreferences!!.navigationBlackPref!!) {
                window.navigationBarColor = appPreferences!!.primaryColorPref
            }
        }
    }

    private fun setScreenElements() {
        val appNameVersion = findViewById(R.id.app_name) as TextView?
        appNameVersion!!.text = resources.getString(R.string.app_name)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right)
    }

}
