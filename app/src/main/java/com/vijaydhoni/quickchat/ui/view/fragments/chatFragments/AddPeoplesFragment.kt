package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.FragmentAddPeoplesBinding
import com.vijaydhoni.quickchat.ui.view.activity.ChatActivity
import com.vijaydhoni.quickchat.ui.view.adapters.UsersRvAdapter
import com.vijaydhoni.quickchat.ui.viewmodel.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddPeoplesFragment : Fragment() {
    private val binding: FragmentAddPeoplesBinding by lazy {
        FragmentAddPeoplesBinding.inflate(layoutInflater)
    }
    private lateinit var userAdapter: UsersRvAdapter
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private var currenUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentUserId()
        setRv()
        chatViewModel.getAllUsers()
        observeUsers()
        onItemClick()
    }

    private fun getCurrentUserId() {
        currenUserId = chatViewModel.getCurrentUserId()
    }

    private fun onItemClick() {
        userAdapter.onClick = {
            val bundle = Bundle()
            bundle.putParcelable("user", it)
            findNavController().navigate(R.id.action_addPeoplesFragment_to_chatFragment, bundle)
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.allUsers.collectLatest { response ->
                    when (response) {

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.usersProgressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.usersProgressBar.visibility = View.GONE
                            val users = response.data
                            users?.let {
                                userAdapter.diff.submitList(it)
                            }
                        }

                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.usersProgressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {}

                    }
                }
            }
        }
    }

    private fun setRv() {
        userAdapter = UsersRvAdapter(currenUserId)
        binding.usersRv.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalItemdecorationRv(12))
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as? ChatActivity
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)?.let { bnv ->
            bnv.visibility = View.VISIBLE
        }
    }


}