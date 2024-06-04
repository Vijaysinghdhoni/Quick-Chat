package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.FragmentSearchBinding
import com.vijaydhoni.quickchat.ui.view.adapters.SearchRvAdapter
import com.vijaydhoni.quickchat.ui.viewmodel.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import com.vijaydhoni.quickchat.util.setStatusBarColour
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var currentUserId: String? = null
    private val binding: FragmentSearchBinding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }
    private lateinit var searchAdapter: SearchRvAdapter
    private val chatViewModel by activityViewModels<ChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewSetup()
        setUpSearchRv()
        binding.mystatusBackBttn.setOnClickListener {
            findNavController().navigateUp()
        }
        searchAdapter.onClick = {
            val bundle = Bundle()
            bundle.putParcelable("user", it)
            findNavController().navigate(R.id.action_searchFragment_to_chatFragment, bundle)
        }
        //  observeSearchQuery()
    }

//    private fun observeSearchQuery() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                chatViewModel.searchedUser.collectLatest { resource ->
//
//                    when (resource) {
//
//                        is Resource.Loading -> {
//                            binding.searchProgressBar.visibility = View.VISIBLE
//                        }
//
//                        is Resource.Success -> {
//                            binding.searchProgressBar.visibility = View.INVISIBLE
//                            val users = resource.data
//                            if (!users.isNullOrEmpty()) {
//                                showSearchRvHideanima()
//                                searchAdapter.differ.submitList(users)
//                            } else {
//                                hideSearchViewsVisibility()
//                                Log.d("Chats", "No data found")
//                            }
//                        }
//
//                        is Resource.Error -> {
//                            binding.searchProgressBar.visibility = View.INVISIBLE
//                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                        else -> {}
//                    }
//
//
//                }
//            }
//        }
//    }


    private fun searchViewSetup() {
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideanimationSearchNow()
                query?.let {
                    observeSearchedUser(it)
                }
                return false
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                MainScope().launch {
                    delay(1200)
                    if (!newText.isNullOrEmpty()) {
                        hideanimationSearchNow()
                        //impliment this
                    }
                }
                return false
            }

        })

        binding.searchView.setOnCloseListener {
            showanimationSearchNowhideallviews()
            false
        }
    }

    private fun observeSearchedUser(it: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.getSearchedUser(it).collectLatest { resource ->

                    when (resource) {

                        is Resource.Loading -> {
                            binding.searchProgressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.searchProgressBar.visibility = View.INVISIBLE
                            val users = resource.data
                            if (!users.isNullOrEmpty()) {
                                showSearchRvHideanima()
                                searchAdapter.differ.submitList(users)
                            } else {
                                hideSearchViewsVisibility()
                                Log.d("Chats", "No data found")
                            }
                        }

                        is Resource.Error -> {
                            binding.searchProgressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        else -> {}
                    }


                }
            }
        }
    }

    private fun hideanimationSearchNow() {
        binding.searchanimationView.visibility = View.GONE
    }

    private fun showanimationSearchNowhideallviews() {
        binding.searchanimationView.visibility = View.VISIBLE
        binding.nodatafoundhanimationView.visibility = View.GONE
        binding.searchRv.visibility = View.GONE
    }

    private fun showSearchRvHideanima() {
        binding.nodatafoundhanimationView.visibility = View.GONE
        binding.searchanimationView.visibility = View.GONE
        binding.searchRv.visibility = View.VISIBLE
    }

    private fun hideSearchViewsVisibility() {
        binding.searchRv.visibility = View.GONE
        binding.searchanimationView.visibility = View.GONE
        binding.nodatafoundhanimationView.visibility = View.VISIBLE
    }

    private fun setUpSearchRv() {
        currentUserId = chatViewModel.getCurrentUserId()
        searchAdapter = SearchRvAdapter(currentUserId)
        binding.searchRv.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalItemdecorationRv(20))
        }
    }


    private fun showKeyboardAutomatically() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        binding.searchView.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        showKeyboardAutomatically()
        val bottomNavigation =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
        bottomNavigation?.visibility = View.INVISIBLE
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }

}