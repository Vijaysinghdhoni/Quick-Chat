package com.vijaydhoni.quickchat.data.models

import com.google.firebase.Timestamp

data class User(
    var phone: String = "",
    var userName: String? = null,
    var createdTimeStrap: Timestamp = Timestamp.now()
)
