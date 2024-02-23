package com.vijaydhoni.quickchat.data.models

import com.google.firebase.Timestamp


data class ChatRoom(
    var chatRoomId: String? = null,
    var usersIds: List<String?> = emptyList(),
    var lastMssgTimeStamp: Timestamp? = null,
    var lastMssgSenderId: String? = null,
    var lastMssg: String = "",
    var lastMssgSeen : Boolean = false
)
