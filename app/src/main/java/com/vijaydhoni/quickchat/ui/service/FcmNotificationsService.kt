package com.vijaydhoni.quickchat.ui.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vijaydhoni.quickchat.data.repositorys.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmNotificationsService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var chatRepository: ChatRepository


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "new msg is ${message.data}")
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isNotEmpty()) {
            scope.launch {
                chatRepository.setUserMessagingToken(token)
            }
            Log.d("FCM", "new token send")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

}