package com.vijaydhoni.quickchat.data.models

import com.google.firebase.Timestamp

data class Message(
    var message: String = "",
    var senderId: String? = null,
    var timeStamp: Timestamp = Timestamp.now(),
    var seen: Boolean = false
)
