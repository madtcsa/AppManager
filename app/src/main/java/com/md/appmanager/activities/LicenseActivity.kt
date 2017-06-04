package com.md.appmanager.activities

import android.os.Bundle

import com.md.appmanager.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsActivity

class LicenseActivity : LibsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        intent = LibsBuilder()
                .withActivityTitle(resources.getString(R.string.settings_license))
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withAutoDetect(true)
                .intent(this)

        super.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right)
    }

}
