package com.davenet.notely.util

import android.content.Context

fun calculateNoOfColumns(context: Context, columnWidthDp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels/displayMetrics.density
        return (screenWidthDp/columnWidthDp + 0.5).toInt()
    }