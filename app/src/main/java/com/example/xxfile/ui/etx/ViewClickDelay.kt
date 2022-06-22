package com.example.xxfile.ui.etx

import android.view.View
import com.example.xxfile.ui.etx.ViewClickDelay.SPACE_TIME
import com.example.xxfile.ui.etx.ViewClickDelay.hash
import com.example.xxfile.ui.etx.ViewClickDelay.lastClickTime

object ViewClickDelay {
    var hash: Int = 0
    var lastClickTime: Long = 0
    var SPACE_TIME: Long = 500
}

fun View.onExecClick(clickAction: () -> Unit) {
    this.setOnClickListener {
        if (this.hashCode() != hash) {
            hash = this.hashCode()
            lastClickTime = System.currentTimeMillis()
            clickAction()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > SPACE_TIME) {
                lastClickTime = System.currentTimeMillis()
                clickAction()
            }
        }
    }
}

fun View.clearClick() {
    this.setOnClickListener(null)
}