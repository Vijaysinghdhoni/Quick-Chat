package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.FragmentRecentChatBinding
import com.vijaydhoni.quickchat.ui.view.adapters.RecentChatRvAdapter
import com.vijaydhoni.quickchat.ui.viewmodels.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import com.vijaydhoni.quickchat.util.setStatusBarColour
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecentChatFragment : Fragment() {
    private val binding: FragmentRecentChatBinding by lazy {
        FragmentRecentChatBinding.inflate(layoutInflater)
    }
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private lateinit var recentsAdapter: RecentChatRvAdapter
    private var currentUserId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentuserId()
        setupRvAdapter()
        searchBttnClick()
        getRecentsChats()
        observeRecentschats()
        recylerviewItemClick()
        recylerviewUserDatacallBack()
    }


    //      story automatic delete after 24 hours
    //      add paging
    // when both users are online then the users message seen or not functionlsity not working


    private fun recylerviewUserDatacallBack() {
        recentsAdapter.userData = { usersIds, user ->


            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    chatViewModel.getUserById(userIds = usersIds).collectLatest {
                        user(it)
                        Log.d("mytag", "user is $it")
                    }
                }

            }

        }


    }

    private fun recylerviewItemClick() {
        recentsAdapter.onItemClick = {
            val bundle = Bundle()
            bundle.putParcelable("user", it)
            findNavController().navigate(R.id.action_recentChatFragment_to_chatFragment, bundle)
        }
    }


    private fun observeRecentschats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.recentsChats.collect { response ->
                    when (response) {

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            val recentsChats = response.data
                            binding.progressBar.visibility = View.INVISIBLE
                            if (recentsChats != null) {
                                recentsAdapter.differ.submitList(recentsChats)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "No recents chats found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is Resource.Error -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                requireContext(),
                                response.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun getRecentsChats() {
        currentUserId?.let {
            chatViewModel.getAllRecentChats(it)
        }
    }

    private fun getCurrentuserId() {
        currentUserId = chatViewModel.getCurrentUserId()
    }

    private fun setupRvAdapter() {
        recentsAdapter = RecentChatRvAdapter(currentUserId, requireContext())
        binding.recentsChatsRv.apply {
            adapter = recentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalItemdecorationRv(4))
        }
    }

    private fun searchBttnClick() {
        binding.mainSearchBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recentChatFragment_to_searchFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.getUnseenChatRooms()
        val bottomNavigation =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
        bottomNavigation?.visibility = View.VISIBLE
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }
}