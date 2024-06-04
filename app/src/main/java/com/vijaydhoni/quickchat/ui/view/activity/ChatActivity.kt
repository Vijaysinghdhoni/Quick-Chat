package com.vijaydhoni.quickchat.ui.view.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHost
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.BuildConfig
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.ActivityChatBinding
import com.vijaydhoni.quickchat.ui.viewmodel.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private val chatViewModel by viewModels<ChatViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        chatViewModel.getCurrentUser()
        observeCurrentUser()
        chatViewModel.setUserActiveOrNot(true)
        chatViewModel.getUnseenChatRooms()
        setBnvMssgsBadge()
        getMessagingTokenAndSet()
        val navHost =
            supportFragmentManager.findFragmentById(R.id.chat_fragment_container) as NavHost
        val navController = navHost.navController
        binding.bottomNavigationChat.setupWithNavController(navController)
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.currentUser.collectLatest { user ->
                    user?.let {
                        Log.d("userTag", " user is ${it.userName}")
                        videoCallingServices(it)
                    }

                }
            }
        }
    }

    private fun setBnvMssgsBadge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.unseenChatRooms.collectLatest { response ->
                    when (response) {

                        is Resource.Success -> {
                            val count = response.data?.size ?: 0
                            val bnv =
                                findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
                            bnv.getOrCreateBadge(R.id.recentChatFragment).apply {
                                number = count
                                backgroundColor = resources.getColor(R.color.g_blue)
                            }
                        }

                        else -> {
                        }

                    }

                }
            }
        }
    }


    private fun videoCallingServices(user: User) {
        val userId = user.userId
        val appID: Long =
            BuildConfig.ZEGO_CLOUD_APP_ID.toLong()
        val appSign =
            BuildConfig.ZEGO_CLOUD_APP_SIGN
        val application = application
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        val notificationConfig = ZegoNotificationConfig()
        notificationConfig.sound = "zego_uikit_sound_call"
        notificationConfig.channelID = "CallInvitation"
        notificationConfig.channelName = "CallInvitation"
        ZegoUIKitPrebuiltCallService.init(
            application,
            appID,
            appSign,
            userId,
            user.userName,
            callInvitationConfig
        )
    }


    private fun getMessagingTokenAndSet() {
        chatViewModel.getAndSetUserMessagingToken()
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.setUserActiveOrNot(true)
    }

    override fun onPause() {
        super.onPause()
        chatViewModel.setUserActiveOrNot(false)
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.setUserActiveOrNot(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.unInit()
        chatViewModel.setUserActiveOrNot(false)
    }
}