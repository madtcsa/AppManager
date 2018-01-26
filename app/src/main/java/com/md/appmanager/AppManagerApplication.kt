package com.md.appmanager

import android.app.Application

import com.md.appmanager.utils.AppPreferences
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.Iconics

class AppManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
        isPro = this.packageName == proPackage
        Iconics.registerFont(GoogleMaterial())
    }

    companion object {
        var appPreferences: AppPreferences? = null
        private var isPro: Boolean = false

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
