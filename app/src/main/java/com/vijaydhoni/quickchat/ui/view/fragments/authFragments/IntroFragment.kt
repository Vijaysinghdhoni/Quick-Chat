package com.vijaydhoni.quickchat.ui.view.fragments.authFragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Intro
import com.vijaydhoni.quickchat.databinding.FragmentIntroBinding
import com.vijaydhoni.quickchat.ui.view.adapters.IntroViewPagerAdapter
import com.vijaydhoni.quickchat.ui.viewmodel.AuthenticationViewModel
import com.vijaydhoni.quickchat.util.setStatusBarColour
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : Fragment() {
    private val binding by lazy {
        FragmentIntroBinding.inflate(layoutInflater)
    }

    private val viewPaggerRvAdapter by lazy {
        IntroViewPagerAdapter()
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val viewModel by activityViewModels<AuthenticationViewModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    requireContext(),
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.nextIntroButtonClicked()
                findNavController().navigate(R.id.action_introFragment_to_loginFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_introFragment_to_loginFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarColour(activity as AppCompatActivity, R.color.white)
        nextButtonClicked()
        setViewPagerRv()
        setNextButtonClick()
    }

    private fun nextButtonClicked() {
        if (viewModel.isNextButtonClicked) {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
        }
    }

    private fun setNextButtonClick() {
        binding.nextBttn.setOnClickListener {
            val currentItem = binding.viewPager.currentItem

            if (currentItem < viewPaggerRvAdapter.itemCount - 1) {
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else if (currentItem == viewPaggerRvAdapter.itemCount - 1) {
                askNotificationPermission()
            } else {
                findNavController().navigate(R.id.action_introFragment_to_loginFragment)
            }
        }

        binding.skipBttn.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    requireContext(),
                    "Notification permission already granted",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.nextIntroButtonClicked()
                findNavController().navigate(R.id.action_introFragment_to_loginFragment)
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Toast.makeText(
                    requireContext(),
                    "Please grant Notification permission",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun setViewPagerRv() {
        binding.viewPager.adapter = viewPaggerRvAdapter
        val introList = mutableListOf(
            Intro(
                "Welcome to our chat app!",
                "Break the ice and forge new connections effortlessly with our chat app.",
                R.drawable.quickchatscreen1
            ),
            Intro(
                "Organize Chats, Unleash Productivity.",
                "Stay on top of your messages with chat folders, keeping your conversations neatly!",
                R.drawable.quickchatscreen2
            ),
            Intro(
                "Chat Privately with End-to-End Encryption",
                "Ensure your conversations are for your eyes only—enjoy !!",
                R.drawable.quickchaatscreen3
            ),
            Intro(
                "Get notified!",
                "Never miss a message! Enable chat notifications to stay instantly informed about new messages, replies, and important updates.",
                R.drawable.quick_chat_screen_notification
            )
        )

        viewPaggerRvAdapter.differ.submitList(introList)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ ->

        }.attach()

    }


}