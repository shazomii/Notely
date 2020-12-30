package com.davenet.notely.util

import android.app.Activity
import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.davenet.notely.R
import com.davenet.notely.domain.NoteEntry
import petrov.kristiyan.colorpicker.ColorPicker
import java.text.SimpleDateFormat
import java.util.*

fun calculateNoOfColumns(context: Context, columnWidthDp: Int): Int {
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()
}

fun hideKeyboard(view: View?, context: Context) {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun selectColor(activity: Activity, note: NoteEntry) {
    val colorPicker = ColorPicker(activity)
    colorPicker.run {
        setRoundColorButton(true)
            .setTitle(activity.getString(R.string.note_color))
            .show()
        setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
            override fun onChooseColor(position: Int, color: Int) {
                note.color = color
                activity.findViewById<ConstraintLayout>(R.id.editNoteLayout)
                    .setBackgroundColor(color)
                activity.findViewById<CardView>(R.id.reminderCard).setCardBackgroundColor(color)
            }

            override fun onCancel() {
                return
            }
        })
    }
}

fun currentDate(): Calendar {
    return Calendar.getInstance()
}

fun formatDate(date: Long): String {
    val dateString = DateUtils.getRelativeTimeSpanString(
        date,
        currentDate().timeInMillis,
        DateUtils.SECOND_IN_MILLIS
    ).toString()
    return when {
        "minute" in dateString -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        }
        " seconds" in dateString -> {
            "now"
        }
        else -> dateString
    }
}

fun formatReminderDate(date: Long): String {
    return SimpleDateFormat("dd MMM, yyyy h:mm a", Locale.getDefault()).format(date)
}

fun formatDateOnly(date: Long): String {
    return SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)
}

fun formatTime(date: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}

val colors = listOf(
    -504764, -740056, -1544140, -2277816, -3246217, -4024195,
    -4224594, -7305542, -7551917, -7583749, -10712898, -10896368, -10965321,
    -11419154, -14654801
)