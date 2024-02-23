package com.vijaydhoni.quickchat.util

import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Fragment.getTimeInFormat(timeStamp: Timestamp): String {
    val currentDate = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply {
        time = timeStamp.toDate()
    }

    return if (currentDate.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
        && currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
    ) {
        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(timeStamp.toDate())
    } else {
        SimpleDateFormat("MMM dd", Locale.ENGLISH).format(timeStamp.toDate())
    }
}