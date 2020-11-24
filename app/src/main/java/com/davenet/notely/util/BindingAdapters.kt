package com.davenet.notely.util

import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("visible")
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("textView:strikeThrough")
fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
    if (strikeThrough) {
        textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = 0
    }
}
