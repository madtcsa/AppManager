package com.md.appmanager

import android.graphics.drawable.Drawable

import java.io.Serializable

class AppInfo : Serializable {
    var name: String? = null
        private set
    var apk: String? = null
        private set
    var version: String? = null
        private set
    var source: String? = null
        private set
    var data: String? = null
        private set
    var icon: Drawable? = null
    var isSystem: Boolean? = null
        private set

    constructor(name: String, apk: String, version: String, source: String, data: String, icon: Drawable, isSystem: Boolean?) {
        this.name = name
        this.apk = apk
        this.version = version
        this.source = source
        this.data = data
        this.icon = icon
        this.isSystem = isSystem
    }

    constructor(string: String) {
        val split = string.split("##".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (split.size == 6) {
            this.name = split[0]
            this.apk = split[1]
            this.version = split[2]
            this.source = split[3]
            this.data = split[4]
            this.isSystem = java.lang.Boolean.getBoolean(split[5])
        }
    }

    override fun toString(): String {
        return "$name##$apk##$version##$source##$data##$isSystem"
    }

}
