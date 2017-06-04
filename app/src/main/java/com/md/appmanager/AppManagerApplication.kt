package com.md.appmanager

import android.app.Application

import com.md.appmanager.utils.AppPreferences
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.Iconics

class AppManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Load Shared Preference
        appPreferences = AppPreferences(this)

        // Check if there is the Pro version
        isPro = this.packageName == proPackage

        // Register custom fonts like this (or also provide a font definition file)
        Iconics.registerFont(GoogleMaterial())
    }

    companion object {
        var appPreferences: AppPreferences? = null
            private set
        private var isPro: Boolean = false

        /**
         * Retrieve ML Manager Pro
         * @return true for ML Manager Pro, false otherwise
         */
        fun isPro(): Boolean? {
            return isPro
        }

        fun setPro(res: Boolean?) {
            isPro = res!!
        }

        val proPackage: String
            get() = "com.md.appmanager"
    }
}
