package com.md.appmanager.adapters

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView

import com.md.appmanager.AppManagerApplication
import com.md.appmanager.activities.AppActivity
import com.md.appmanager.AppInfo
import com.md.appmanager.R
import com.md.appmanager.activities.MainActivity
import com.md.appmanager.async.ExtractFileInBackground
import com.md.appmanager.utils.AppPreferences
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog
import com.md.appmanager.view.material.ButtonFlat
import java.io.ByteArrayOutputStream

import java.util.ArrayList

class AppAdapter(// AppAdapter variables
        private var appList: MutableList<AppInfo>?, private val context: Context) : RecyclerView.Adapter<AppAdapter.AppViewHolder>(), Filterable {
    // Load Settings
    private val appPreferences: AppPreferences
    private var appListSearch: List<AppInfo>? = null

    init {
        this.appPreferences = AppManagerApplication.appPreferences!!
    }

    override fun getItemCount(): Int {
        return appList!!.size
    }

    fun clear() {
        appList!!.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(appViewHolder: AppViewHolder, i: Int) {
        val appInfo = appList!![i]
        appViewHolder.vName.text = appInfo.name
        appViewHolder.vApk.text = appInfo.apk
        appViewHolder.vIcon.setImageDrawable(appInfo.icon)

        setButtonEvents(appViewHolder, appInfo)

    }

    private fun setButtonEvents(appViewHolder: AppViewHolder, appInfo: AppInfo) {
        val appExtract = appViewHolder.vExtract
        val appShare = appViewHolder.vShare
        val appIcon = appViewHolder.vIcon
        val cardView = appViewHolder.vCard

        appExtract.setBackgroundColor(appPreferences.primaryColorPref)
        appShare.setBackgroundColor(appPreferences.primaryColorPref)

        appExtract.setOnClickListener {
            val dialog = UtilsDialog.showTitleContentWithProgress(context, String.format(context.resources.getString(R.string.dialog_saving), appInfo.name), context.resources.getString(R.string.dialog_saving_description))
            ExtractFileInBackground(context, dialog, appInfo).execute()
        }
        appShare.setOnClickListener {
            UtilsApp.copyFile(appInfo)
            val shareIntent = UtilsApp.getShareIntent(UtilsApp.getOutputFilename(appInfo))
            context.startActivity(Intent.createChooser(shareIntent, String.format(context.resources.getString(R.string.send_to), appInfo.name)))
        }

        cardView.setOnClickListener {
            val activity = context as Activity

            val intent = Intent(context, AppActivity::class.java)
            intent.putExtra("app_name", appInfo.name)
            intent.putExtra("app_apk", appInfo.apk)
            intent.putExtra("app_version", appInfo.version)
            intent.putExtra("app_source", appInfo.source)
            intent.putExtra("app_data", appInfo.data)
            val bitmap = (appInfo.icon as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos)
            val bitmapByte = baos.toByteArray()
            intent.putExtra("app_icon", bitmapByte)
            intent.putExtra("app_isSystem", appInfo.isSystem)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val transitionName = context.getResources().getString(R.string.transition_app_icon)

                val transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, appIcon, transitionName)
                context.startActivity(intent, transitionActivityOptions.toBundle())
            } else {
                context.startActivity(intent)
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back)
            }
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val oReturn = Filter.FilterResults()
                val results = ArrayList<AppInfo>()
                if (appListSearch == null) {
                    appListSearch = appList
                }
                if (charSequence != null) {
                    if (appListSearch != null && appListSearch!!.isNotEmpty()) {
                        for (appInfo in appListSearch!!) {
                            if (appInfo.name!!.toLowerCase().contains(charSequence.toString())) {
                                results.add(appInfo)
                            }
                        }
                    }
                    oReturn.values = results
                    oReturn.count = results.size
                }
                return oReturn
            }

            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                if (filterResults.count > 0) {
                    MainActivity.setResultsMessage(false)
                } else {
                    MainActivity.setResultsMessage(true)
                }
                appList = filterResults.values as ArrayList<AppInfo>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AppViewHolder {
        val appAdapterView = LayoutInflater.from(viewGroup.context).inflate(R.layout.app_layout, viewGroup, false)
        return AppViewHolder(appAdapterView)
    }

    class AppViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var vName: TextView
        var vApk: TextView
        var vIcon: ImageView
        var vExtract: ButtonFlat
        var vShare: ButtonFlat
        var vCard: CardView

        init {
            vName = v.findViewById(R.id.txtName) as TextView
            vApk = v.findViewById(R.id.txtApk) as TextView
            vIcon = v.findViewById(R.id.imgIcon) as ImageView
            vExtract = v.findViewById(R.id.btnExtract) as ButtonFlat
            vShare = v.findViewById(R.id.btnShare) as ButtonFlat
            vCard = v.findViewById(R.id.app_card) as CardView

        }
    }

}
