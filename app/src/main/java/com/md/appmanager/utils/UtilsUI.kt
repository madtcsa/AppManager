package com.md.appmanager.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

import com.md.appmanager.AppManagerApplication
import com.md.appmanager.activities.AboutActivity
import com.md.appmanager.R
import com.md.appmanager.activities.SettingsActivity
import com.md.appmanager.adapters.AppAdapter
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem

import java.util.Calendar
import android.graphics.drawable.Drawable
import android.content.res.ColorStateList
import android.content.res.Resources
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.TypedValue




object UtilsUI {

    fun darker(color: Int, factor: Double): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        return Color.argb(a, Math.max((r * factor).toInt(), 0), Math.max((g * factor).toInt(), 0), Math.max((b * factor).toInt(), 0))
    }

    fun setNavigationDrawer(activity: Activity, context: Context, toolbar: Toolbar,
                            appAdapter: AppAdapter?, appSystemAdapter: AppAdapter?, appFavoriteAdapter: AppAdapter?,
                            appHiddenAdapter: AppAdapter?, recyclerView: RecyclerView): Drawer {
        val loadingLabel = "..."
        val header: Int
        val appPreferences = AppManagerApplication.appPreferences
        val apps: String
        val systemApps: String
        val favoriteApps: String
        val hiddenApps: String

        if (dayOrNight == 1) {
            header = R.drawable.header_day
        } else {
            header = R.drawable.header_night
        }

        if (appAdapter != null) {
            apps = Integer.toString(appAdapter.itemCount)
        } else {
            apps = loadingLabel
        }
        if (appSystemAdapter != null) {
            systemApps = Integer.toString(appSystemAdapter.itemCount)
        } else {
            systemApps = loadingLabel
        }
        if (appFavoriteAdapter != null) {
            favoriteApps = Integer.toString(appFavoriteAdapter.itemCount)
        } else {
            favoriteApps = loadingLabel
        }
        if (appHiddenAdapter != null) {
            hiddenApps = Integer.toString(appHiddenAdapter.itemCount)
        } else {
            hiddenApps = loadingLabel
        }

        val headerResult = AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(header)
                .build()

        val badgeColor = ContextCompat.getColor(context, R.color.divider)
        val badgeStyle = BadgeStyle(badgeColor, badgeColor).withTextColor(Color.GRAY)

        val drawerBuilder = DrawerBuilder()
        drawerBuilder.withActivity(activity)
        drawerBuilder.withToolbar(toolbar)
        drawerBuilder.withAccountHeader(headerResult)
        if (appPreferences != null) {
            drawerBuilder.withStatusBarColor(UtilsUI.darker(appPreferences.primaryColorPref, 0.8))
        }

        if (AppManagerApplication.isPro()!!) {
            drawerBuilder.addDrawerItems(
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_apps)).withIcon(GoogleMaterial.Icon.gmd_phone_android).withBadge(apps).withBadgeStyle(badgeStyle).withIdentifier(1),
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_system_apps)).withIcon(GoogleMaterial.Icon.gmd_android).withBadge(systemApps).withBadgeStyle(badgeStyle).withIdentifier(2),
                    DividerDrawerItem(),
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_favorites)).withIcon(GoogleMaterial.Icon.gmd_star).withBadge(favoriteApps).withBadgeStyle(badgeStyle).withIdentifier(3),
                    DividerDrawerItem(),
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_hidden_apps)).withIcon(GoogleMaterial.Icon.gmd_visibility_off).withBadge(hiddenApps).withBadgeStyle(badgeStyle).withIdentifier(4),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withName(context.resources.getString(R.string.action_settings)).withIcon(GoogleMaterial.Icon.gmd_settings).withSelectable(false).withIdentifier(6),
                    SecondaryDrawerItem().withName(context.resources.getString(R.string.action_about)).withIcon(GoogleMaterial.Icon.gmd_info).withSelectable(false).withIdentifier(7))
        } else {
            drawerBuilder.addDrawerItems(
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_apps)).withIcon(GoogleMaterial.Icon.gmd_phone_android).withBadge(apps).withBadgeStyle(badgeStyle).withIdentifier(1),
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_system_apps)).withIcon(GoogleMaterial.Icon.gmd_android).withBadge(systemApps).withBadgeStyle(badgeStyle).withIdentifier(2),
                    DividerDrawerItem(),
                    PrimaryDrawerItem().withName(context.resources.getString(R.string.action_favorites)).withIcon(GoogleMaterial.Icon.gmd_star).withBadge(favoriteApps).withBadgeStyle(badgeStyle).withIdentifier(3),
                    DividerDrawerItem(),
                    SecondaryDrawerItem().withName(context.resources.getString(R.string.action_buy)).withIcon(GoogleMaterial.Icon.gmd_shop).withBadge(context.resources.getString(R.string.action_buy_description)).withSelectable(false).withIdentifier(5),
                    SecondaryDrawerItem().withName(context.resources.getString(R.string.action_settings)).withIcon(GoogleMaterial.Icon.gmd_settings).withSelectable(false).withIdentifier(6),
                    SecondaryDrawerItem().withName(context.resources.getString(R.string.action_about)).withIcon(GoogleMaterial.Icon.gmd_info).withSelectable(false).withIdentifier(7))
        }

        drawerBuilder.withOnDrawerItemClickListener { view, position, iDrawerItem ->
            when (iDrawerItem.identifier) {
                1 -> recyclerView.adapter = appAdapter
                2 -> recyclerView.adapter = appSystemAdapter
                3 -> recyclerView.adapter = appFavoriteAdapter
                4 -> recyclerView.adapter = appHiddenAdapter
                5 -> UtilsDialog.showProFeatures(context)
                6 -> context.startActivity(Intent(context, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                7 -> context.startActivity(Intent(context, AboutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                else -> {
                }
            }

            false
        }

        return drawerBuilder.build()
    }

    val dayOrNight: Int
        get() {
            val actualHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            if (actualHour in 8..18) {
                return 1
            } else {
                return 0
            }
        }

    fun tintDrawable(drawable: Drawable, colors: ColorStateList): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTintList(wrappedDrawable, colors)
        return wrappedDrawable
    }

}
