package com.md.appmanager.listeners

import android.support.v7.widget.RecyclerView

abstract class HidingScrollListener : RecyclerView.OnScrollListener() {
    private var scrolledDistance = 0
    private var controlsVisible = true

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide()
            controlsVisible = false
            scrolledDistance = 0
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow()
            controlsVisible = true
            scrolledDistance = 0
        }

        if (controlsVisible && dy > 0 || !controlsVisible && dy < 0) {
            scrolledDistance += dy
        }
    }

    abstract fun onHide()
    abstract fun onShow()

    companion object {
        private val HIDE_THRESHOLD = 20
    }

}
