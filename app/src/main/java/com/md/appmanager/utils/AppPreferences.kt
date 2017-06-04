package com.md.appmanager.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.md.appmanager.R

import java.util.HashSet

class AppPreferences(private val context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor

    init {
        this.editor = sharedPreferences.edit()
    }

    var rootStatus: Int
        get() = sharedPreferences.getInt(KeyIsRooted, 0)
        set(rootStatus) {
            editor.putInt(KeyIsRooted, rootStatus)
            editor.commit()
        }

    val primaryColorPref: Int
        get() = sharedPreferences.getInt(KeyPrimaryColor, context.resources.getColor(R.color.primary))

    fun setPrimaryColorPref(res: Int?) {
        editor.putInt(KeyPrimaryColor, res!!)
        editor.commit()
    }

    val fabColorPref: Int
        get() = sharedPreferences.getInt(KeyFABColor, context.resources.getColor(R.color.fab))

    fun setFABColorPref(res: Int?) {
        editor.putInt(KeyFABColor, res!!)
        editor.commit()
    }

    var navigationBlackPref: Boolean?
        get() = sharedPreferences.getBoolean(KeyNavigationBlack, false)
        set(res) {
            editor.putBoolean(KeyNavigationBlack, res!!)
            editor.commit()
        }

    var fabShowPref: Boolean?
        get() = sharedPreferences.getBoolean(KeyFABShow, false)
        set(res) {
            editor.putBoolean(KeyFABShow, res!!)
            editor.commit()
        }

    var customFilename: String
        get() = sharedPreferences.getString(KeyCustomFilename, "1")
        set(res) {
            editor.putString(KeyCustomFilename, res)
            editor.commit()
        }

    var sortMode: String
        get() = sharedPreferences.getString(KeySortMode, "1")
        set(res) {
            editor.putString(KeySortMode, res)
            editor.commit()
        }

    var customPath: String
        get() = sharedPreferences.getString(KeyCustomPath, UtilsApp.defaultAppFolder.getPath())
        set(path) {
            editor.putString(KeyCustomPath, path)
            editor.commit()
        }

    var favoriteApps: MutableSet<String>
        get() = sharedPreferences.getStringSet(KeyFavoriteApps, HashSet<String>())
        set(favoriteApps) {
            editor.remove(KeyFavoriteApps)
            editor.commit()
            editor.putStringSet(KeyFavoriteApps, favoriteApps)
            editor.commit()
        }

    var hiddenApps: MutableSet<String>
        get() = sharedPreferences.getStringSet(KeyHiddenApps, HashSet<String>())
        set(hiddenApps) {
            editor.remove(KeyHiddenApps)
            editor.commit()
            editor.putStringSet(KeyHiddenApps, hiddenApps)
            editor.commit()
        }

    companion object {

        val KeyPrimaryColor = "prefPrimaryColor"
        val KeyFABColor = "prefFABColor"
        val KeyFABShow = "prefFABShow"
        val KeyNavigationBlack = "prefNavigationBlack"
        val KeyCustomFilename = "prefCustomFilename"
        val KeySortMode = "prefSortMode"
        val KeyIsRooted = "prefIsRooted"
        val KeyCustomPath = "prefCustomPath"

        // List
        val KeyFavoriteApps = "prefFavoriteApps"
        val KeyHiddenApps = "prefHiddenApps"
    }

}
