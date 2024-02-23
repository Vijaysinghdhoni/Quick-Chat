package com.vijaydhoni.quickchat.data.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var phone: String = "",
    var userName: String? = null,
    var createdTimeStrap: Timestamp = Timestamp.now(),
    var userId: String? = null,
    var imagePath: String = "",
    var isUserActive: Boolean = false,
    var lastSeenTime: Timestamp = Timestamp.now(),
    var fcmToken: String = "",
    var userStatus: String = "No status"
) : Parcelable
