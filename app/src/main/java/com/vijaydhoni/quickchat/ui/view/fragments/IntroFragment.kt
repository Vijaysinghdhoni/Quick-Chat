package com.vijaydhoni.quickchat.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Intro
import com.vijaydhoni.quickchat.databinding.FragmentIntroBinding
import com.vijaydhoni.quickchat.ui.view.adapters.IntroViewPagerAdapter
import com.vijaydhoni.quickchat.util.setStatusBarColour


class IntroFragment : Fragment() {
    private val binding by lazy {
        FragmentIntroBinding.inflate(layoutInflater)
    }

    private val viewPaggerRvAdapter by lazy {
        IntroViewPagerAdapter()
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
        setViewPagerRv()
        setNextButtonClick()
    }

    private fun setNextButtonClick() {
        binding.nextBttn.setOnClickListener {
            val currentItem = binding.viewPager.currentItem

            if (currentItem < viewPaggerRvAdapter.itemCount - 1) {
                binding.viewPager.setCurrentItem(currentItem + 1, true)
            } else {
                findNavController().navigate(R.id.action_introFragment_to_loginFragment)
            }
        }

        binding.skipBttn.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
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
            )
        )

        viewPaggerRvAdapter.differ.submitList(introList)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ ->

        }.attach()

    }


}