package com.vijaydhoni.quickchat.ui.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHost
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.ActivityChatBinding
import com.vijaydhoni.quickchat.ui.viewmodels.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
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
        chatViewModel.setUserActiveOrNot(true)
        chatViewModel.getUnseenChatRooms()
        setBnvMssgsBadge()
        getMessagingTokenAndSet()
        val navHost =
            supportFragmentManager.findFragmentById(R.id.chat_fragment_container) as NavHost
        val navController = navHost.navController
        binding.bottomNavigationChat.setupWithNavController(navController)
    }

    private fun setBnvMssgsBadge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chatViewModel.unseenChatRooms.collectLatest { response ->
                    when(response){

                        is Resource.Success -> {
                            val count = response.data?.size ?: 0
                            val bnv = findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
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
        chatViewModel.setUserActiveOrNot(false)
    }
}