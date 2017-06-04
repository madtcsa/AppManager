package com.md.appmanager.utils

import android.os.Build

import com.md.appmanager.AppManagerApplication

import java.io.File

object UtilsRoot {

    private val ROOT_STATUS_NOT_CHECKED = 0
    private val ROOT_STATUS_ROOTED = 1
    private val ROOT_STATUS_NOT_ROOTED = 2

    val isRooted: Boolean
        get() {
            val rootStatus = AppManagerApplication.appPreferences!!.rootStatus
            var isRooted = false
            if (rootStatus == ROOT_STATUS_NOT_CHECKED) {
                isRooted = isRootByBuildTag || isRootedByFileSU || isRootedByExecutingCommand
                AppManagerApplication.appPreferences!!.rootStatus = if (isRooted) ROOT_STATUS_ROOTED else ROOT_STATUS_NOT_ROOTED
            } else if (rootStatus == ROOT_STATUS_ROOTED) {
                isRooted = true
            }
            return isRooted
        }

    val isRootByBuildTag: Boolean
        get() {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

    val isRootedByFileSU: Boolean
        get() {
            try {
                val file = File("/system/app/Superuser.apk")
                if (file.exists()) {
                    return true
                }
            } catch (e1: Exception) {
            }

            return false
        }

    val isRootedByExecutingCommand: Boolean
        get() = canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su")
                || canExecuteCommand("which su")

    fun removeWithRootPermission(directory: String): Boolean {
        var status = false
        try {
            val command = arrayOf("su", "-c", "rm -rf " + directory)
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val i = process.exitValue()
            if (i == 0) {
                status = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return status
    }

    fun hideWithRootPermission(apk: String, hidden: Boolean?): Boolean {
        var status = false
        try {
            val command: Array<String>
            if (hidden!!) {
                command = arrayOf("su", "-c", "pm unhide " + apk + "\n")
            } else {
                command = arrayOf("su", "-c", "pm hide " + apk + "\n")
            }

            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val i = process.exitValue()
            if (i == 0) {
                status = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return status
    }

    fun uninstallWithRootPermission(source: String): Boolean {
        var status = false
        try {
            val command_write = arrayOf("su", "-c", "mount -o rw,remount /system\n")
            val command_delete = arrayOf("su", "-c", "rm -r " + "/" + source + "\n")
            val command_read = arrayOf("su", "-c", "mount -o ro,remount /system\n")

            var process = Runtime.getRuntime().exec(command_write)
            process.waitFor()
            var i = process.exitValue()
            if (i == 0) {
                process = Runtime.getRuntime().exec(command_delete)
                process.waitFor()
                i = process.exitValue()
                if (i == 0) {
                    process = Runtime.getRuntime().exec(command_read)
                    process.waitFor()
                    i = process.exitValue()
                    if (i == 0) {
                        status = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return status
    }

    fun rebootSystem(): Boolean {
        var status = false
        try {
            val command = arrayOf("su", "-c", "reboot\n")

            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val i = process.exitValue()
            if (i == 0) {
                status = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return status
    }

    fun getFolderSizeInMB(directory: String): Long {
        val f = File(directory)
        var size: Long = 0
        if (f.isDirectory) {
            for (file in f.listFiles()) {
                size += getFolderSizeInMB(file.absolutePath)
            }
        } else {
            size = f.length() / 1024 / 2024
        }

        return size
    }

    private fun canExecuteCommand(command: String): Boolean {
        var isExecuted: Boolean
        try {
            Runtime.getRuntime().exec(command)
            isExecuted = true
        } catch (e: Exception) {
            isExecuted = false
        }

        return isExecuted
    }
}
