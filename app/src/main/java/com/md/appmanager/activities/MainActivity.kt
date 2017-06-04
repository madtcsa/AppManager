package com.md.appmanager.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import android.content.pm.ApplicationInfo
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.ProgressBar

import com.md.appmanager.AppInfo
import com.md.appmanager.AppManagerApplication
import com.md.appmanager.R
import com.md.appmanager.adapters.AppAdapter
import com.md.appmanager.utils.AppPreferences
import com.md.appmanager.utils.UtilsApp
import com.md.appmanager.utils.UtilsDialog
import com.md.appmanager.utils.UtilsUI
import com.mikepenz.materialdrawer.Drawer
import com.pnikosis.materialishprogress.ProgressWheel
import com.yalantis.phoenix.PullToRefreshView

import java.io.File
import java.util.ArrayList
import java.util.Collections

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private val TAG = MainActivity::class.java.simpleName
    // Load Settings
    private var appPreferences: AppPreferences? = null

    // General variables
    private var appList: MutableList<AppInfo>? = null
    private var appSystemList: MutableList<AppInfo>? = null
    private var appHiddenList: MutableList<AppInfo>? = null

    private var appAdapter: AppAdapter? = null
    private var appSystemAdapter: AppAdapter? = null
    private var appFavoriteAdapter: AppAdapter? = null
    private var appHiddenAdapter: AppAdapter? = null

    // Configuration variables
    private var doubleBackToExitPressedOnce: Boolean? = false
    private var toolbar: Toolbar? = null
    private var activity: Activity? = null
    private var context: Context? = null
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var drawer: Drawer? = null
    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var mHandler = Handler()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.appPreferences = AppManagerApplication.appPreferences
        this.activity = this
        this.context = this

        setInitialConfiguration()
        checkAndAddPermissions(activity as MainActivity)
        setAppDir()

        recyclerView = findViewById(R.id.appList) as RecyclerView?
        fastScroller = findViewById(R.id.fast_scroller) as VerticalRecyclerViewFastScroller?
        progress = findViewById(R.id.progress) as ProgressBar?
        noResults = findViewById(R.id.noResults) as LinearLayout?

        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = linearLayoutManager

        drawer = UtilsUI.setNavigationDrawer((context as Activity?)!!, context as Activity, toolbar!!,
                appAdapter, appSystemAdapter, appFavoriteAdapter, appHiddenAdapter, recyclerView!!)

        progress!!.visibility = View.VISIBLE
        getInstalledApps()
    }

    private fun setInitialConfiguration() {
        toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(R.string.app_name)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = UtilsUI.darker(appPreferences!!.primaryColorPref, 0.8)
            toolbar!!.setBackgroundColor(appPreferences!!.primaryColorPref)
            if (appPreferences!!.navigationBlackPref!!) {
                window.navigationBarColor = appPreferences!!.primaryColorPref
            }
        }
    }

    private fun getInstalledApps() {
        Thread(Runnable {
            kotlin.run {
                appList = ArrayList<AppInfo>()
                appSystemList = ArrayList<AppInfo>()
                appHiddenList = ArrayList<AppInfo>()

                val packageManager = packageManager
                val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                val hiddenApps = appPreferences!!.hiddenApps
                // Get Sort Mode
                when (appPreferences!!.sortMode) {
                    "2" ->
                        // Comparator by Size
                        Collections.sort(packages, { p1, p2 ->
                            val size1 = File(p1.applicationInfo.sourceDir).length()
                            val size2 = File(p2.applicationInfo.sourceDir).length()
                            size2.compareTo(size1)
                        })
                    "3" ->
                        // Comparator by Installation Date (default)
                        Collections.sort(packages, { p1, p2 -> java.lang.Long.toString(p2.firstInstallTime).compareTo(java.lang.Long.toString(p1.firstInstallTime)) })
                    "4" ->
                        // Comparator by Last Update
                        Collections.sort(packages, { p1, p2 -> java.lang.Long.toString(p2.lastUpdateTime).compareTo(java.lang.Long.toString(p1.lastUpdateTime)) })
                    else ->
                        // Comparator by Name (default)
                        Collections.sort(packages, { p1, p2 ->
                            packageManager.getApplicationLabel(p1.applicationInfo).toString().toLowerCase().
                                    compareTo(packageManager.getApplicationLabel(p2.applicationInfo).toString().toLowerCase())
                        })
                }

                // Installed & System Apps
                packages
                        .filterNot { packageManager.getApplicationLabel(it.applicationInfo) == "" || it.packageName == "" }
                        .forEach {
                            if (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                                try {
                                    // Non System Apps
                                    val tempApp = AppInfo(packageManager.getApplicationLabel(it.applicationInfo).toString(),
                                            it.packageName, it.versionName, it.applicationInfo.sourceDir,
                                            it.applicationInfo.dataDir, packageManager.getApplicationIcon(it.applicationInfo), false)
                                    appList!!.add(tempApp)
                                } catch (e: OutOfMemoryError) {
                                    val tempApp = AppInfo(packageManager.getApplicationLabel(it.applicationInfo).toString(),
                                            it.packageName, it.versionName, it.applicationInfo.sourceDir,
                                            it.applicationInfo.dataDir, resources.getDrawable(R.drawable.ic_android), false)
                                    appList!!.add(tempApp)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            } else {
                                try {
                                    // System Apps
                                    val tempApp = AppInfo(packageManager.getApplicationLabel(it.applicationInfo).toString(),
                                            it.packageName, it.versionName, it.applicationInfo.sourceDir, it.applicationInfo.dataDir,
                                            packageManager.getApplicationIcon(it.applicationInfo), true)
                                    appSystemList!!.add(tempApp)
                                } catch (e: OutOfMemoryError) {
                                    val tempApp = AppInfo(packageManager.getApplicationLabel(it.applicationInfo).toString(), it.packageName,
                                            it.versionName, it.applicationInfo.sourceDir, it.applicationInfo.dataDir, resources.getDrawable(R.drawable.ic_android), false)
                                    appSystemList!!.add(tempApp)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                        }
                Log.d(TAG, "appList size:: " + (appList as ArrayList<AppInfo>).size)
                mHandler.post({
                    appAdapter = AppAdapter(appList, context!!)
                    appSystemAdapter = AppAdapter(appSystemList, context!!)
                    appFavoriteAdapter = AppAdapter(getFavoriteList(appList!!, appSystemList!!), context!!)
                    appHiddenAdapter = AppAdapter(appHiddenList, context!!)

                    fastScroller!!.visibility = View.VISIBLE
                    recyclerView!!.adapter = appAdapter
                    progress!!.visibility = View.GONE
                    searchItem!!.isVisible = true

                    fastScroller!!.setRecyclerView(recyclerView)
                    recyclerView!!.setOnScrollListener(fastScroller!!.onScrollListener)
                    drawer!!.closeDrawer()
                    drawer = UtilsUI.setNavigationDrawer((context as Activity?)!!, context as Activity, toolbar!!,
                            appAdapter, appSystemAdapter, appFavoriteAdapter, appHiddenAdapter, recyclerView!!)
                })
            }
        }).start()
    }

    private fun setPullToRefreshView(pullToRefreshView: PullToRefreshView) {
        pullToRefreshView.setOnRefreshListener {
            appAdapter!!.clear()
            appSystemAdapter!!.clear()
            appFavoriteAdapter!!.clear()
            recyclerView!!.adapter = null
            getInstalledApps()

            pullToRefreshView.postDelayed({ pullToRefreshView.setRefreshing(false) }, 2000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkAndAddPermissions(activity: Activity) {
        UtilsApp.checkPermissions(activity)
    }

    private fun setAppDir() {
        val appDir = UtilsApp.appFolder
        if (!appDir.exists()) {
            appDir.mkdir()
        }
    }

    private fun getFavoriteList(appList: List<AppInfo>, appSystemList: List<AppInfo>): MutableList<AppInfo> {
        val res = ArrayList<AppInfo>()

        for (app in appList) {
            if (UtilsApp.isAppFavorite(app.apk!!, appPreferences!!.favoriteApps)!!) {
                res.add(app)
            }
        }
        for (app in appSystemList) {
            if (UtilsApp.isAppFavorite(app.apk!!, appPreferences!!.favoriteApps)!!) {
                res.add(app)
            }
        }

        return res
    }

    override fun onQueryTextChange(search: String): Boolean {
        if (search.isEmpty()) {
            (recyclerView!!.adapter as AppAdapter).filter.filter("")
        } else {
            (recyclerView!!.adapter as AppAdapter).filter.filter(search.toLowerCase())
        }

        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        searchItem = menu.findItem(R.id.action_search)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView!!.setOnQueryTextListener(this)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()))

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_READ -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    UtilsDialog.showTitleContent(context!!, resources.getString(R.string.dialog_permissions), resources.getString(R.string.dialog_permissions_description))
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen) {
            drawer!!.closeDrawer()
        } else if (searchItem!!.isVisible && !searchView!!.isIconified) {
            searchView!!.onActionViewCollapsed()
        } else {
            if (doubleBackToExitPressedOnce!!) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, R.string.tap_exit, Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    companion object {
        private val MY_PERMISSIONS_REQUEST_WRITE_READ = 1
        private var fastScroller: VerticalRecyclerViewFastScroller? = null
        private var noResults: LinearLayout? = null

        fun setResultsMessage(result: Boolean?) {
            if (result!!) {
                noResults!!.visibility = View.VISIBLE
                fastScroller!!.visibility = View.GONE
            } else {
                noResults!!.visibility = View.GONE
                fastScroller!!.visibility = View.VISIBLE
            }
        }
    }

}
