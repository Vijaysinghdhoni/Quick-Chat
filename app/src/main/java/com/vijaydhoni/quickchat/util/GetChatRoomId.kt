package com.vijaydhoni.quickchat.util

fun getChatRoomId(usrId1: String, userId2: String): String {
    return if (usrId1.hashCode() < userId2.hashCode()) usrId1 + "_" + userId2 else
        userId2 + "_" + usrId1
}