package com.valeo.app.lofapk.utils

import android.view.View
import android.widget.TextView



fun View.showFab() {
    if (!this.isShown) {
        this.visibility = View.VISIBLE
        this.isClickable = true
    }
}

fun View.hideFab() {
    if (this.isShown) {
        this.visibility = View.GONE
        this.isClickable = false
    }
}

fun TextView.hideTip() {
    if (this.isShown) {
        this.visibility = TextView.INVISIBLE
    }
}

fun TextView.showTip() {
    if (this.isShown) {
        this.visibility = TextView.VISIBLE
    }
}