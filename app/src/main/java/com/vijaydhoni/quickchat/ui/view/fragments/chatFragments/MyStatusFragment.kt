package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.app.AlertDialog
import android.graphics.Color
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Story
import com.vijaydhoni.quickchat.data.models.UserStory
import com.vijaydhoni.quickchat.databinding.FragmentMyStatusBinding
import com.vijaydhoni.quickchat.ui.view.activity.ChatActivity
import com.vijaydhoni.quickchat.ui.view.adapters.MyStatusAdapter
import com.vijaydhoni.quickchat.ui.viewmodels.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import com.vijaydhoni.quickchat.util.getTimeInFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.util.ArrayList

@AndroidEntryPoint
class MyStatusFragment : Fragment() {
    private val binding by lazy {
        FragmentMyStatusBinding.inflate(layoutInflater)
    }
    private lateinit var statusAdapter: MyStatusAdapter
    private var alertDialog: AlertDialog? = null
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity as? ChatActivity
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)?.let { bnv ->
            bnv.visibility = View.GONE
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
        val args by navArgs<MyStatusFragmentArgs>()
        val userStory = args.myStorys
        setUpRv(userStory.stories)
        statusAdapter.differ.submitList(userStory.stories)
        backButton()
        onRvStoryClick(userStory)
        onRvDeleteClick()
        observeDeleteStory()
    }


    private fun observeDeleteStory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.deleteUserStory.collectLatest { response ->
                    when (response) {

                        is Resource.Loading -> {
                            showProgressBar()
                        }

                        is Resource.Success -> {
                            dismissProgressBar()
                            Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT)
                                .show()
                        }

                        is Resource.Error -> {
                            dismissProgressBar()
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {}

                    }
                }
            }
        }
    }

    private fun onRvDeleteClick() {
        statusAdapter.onDeleteClick = {
            chatViewModel.deleteStory(it)
        }
    }

    private fun onRvStoryClick(userStory: UserStory) {
        statusAdapter.onStatusClick = {
            setStoriesView(it, userStory)
        }
    }

    private fun showProgressBar() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_box_layout, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun dismissProgressBar() {
        alertDialog?.dismiss()
    }

    private fun setStoriesView(story: Story, userStory: UserStory) {
        val myStories = ArrayList<MyStory>()
        myStories.add(MyStory(story.storyImageUrl))

        StoryView.Builder(requireActivity().supportFragmentManager)
            .setStoriesList(myStories)
            .setStoryDuration(8000).setTitleText(userStory.user?.userName)
            .setSubtitleText(getTimeInFormat(story.storyCreatedTimestamp!!))
            .setOnStoryChangedCallback {
                chatViewModel.setUserStorySeen(story)
            }
            .setTitleLogoUrl(userStory.user?.imagePath)
            .setStoryClickListeners(object : StoryClickListeners {
                override fun onDescriptionClickListener(position: Int) {}
                override fun onTitleIconClickListener(position: Int) {}
            })
            .build()
            .show()
    }

    private fun backButton() {
        binding.mystatusBackBttn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUpRv(story: List<Story>) {
        statusAdapter = MyStatusAdapter(story[0].userID!!, requireContext())
        binding.myStorieRv.apply {
            adapter = statusAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalItemdecorationRv(24))
        }
    }


}