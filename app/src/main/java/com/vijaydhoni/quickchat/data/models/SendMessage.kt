package com.vijaydhoni.quickchat.data.models

data class SendMessage(
    val to: String?,
    val notification: NotificationBody
)

data class NotificationBody(
    val title: String,
    val body: String
)
