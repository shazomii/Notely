package com.davenet.notely.util

import android.content.Context
import android.text.format.DateUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.davenet.notely.R
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.error_dialog.view.*
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

fun showErrorDialog(message: String?, context: Context, activity: FragmentActivity) {
    if (message != null) {
        val builder = AlertDialog.Builder(context)
        val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(context).inflate(R.layout.error_dialog, viewGroup, false)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialogView.apply {
            dialogMessage.text = message
            dismissDialogButton.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}

fun setupLoadingDialog(context: Context, activity: FragmentActivity): AlertDialog {
    val builder = AlertDialog.Builder(context)
    val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
    val dialogView: View =
        LayoutInflater.from(context).inflate(R.layout.loading_dialog, viewGroup, false)
    builder.setView(dialogView)
    return builder.create()
}

fun inputValidation(email: TextInputEditText, password: TextInputEditText): Boolean {
    return when {
        email.text.toString().isEmpty() -> {
            email.error = "Please enter email"
            email.requestFocus()
            true
        }
        !Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches() -> {
            email.error = "Please enter valid email"
            email.requestFocus()
            true
        }
        password.text.toString().isEmpty() -> {
            password.error = "Please enter password"
            password.requestFocus()
            true
        }
        else -> false
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

val colors = listOf(
    -504764, -740056, -1544140, -2277816, -3246217, -4024195,
    -4224594, -7305542, -7551917, -7583749, -10712898, -10896368, -10965321,
    -11419154, -14654801
)