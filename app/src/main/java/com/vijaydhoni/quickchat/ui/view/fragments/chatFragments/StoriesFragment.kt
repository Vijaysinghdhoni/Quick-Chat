package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Story
import com.vijaydhoni.quickchat.data.models.UserStory
import com.vijaydhoni.quickchat.databinding.FragmentStoriesBinding
import com.vijaydhoni.quickchat.ui.view.activity.ChatActivity
import com.vijaydhoni.quickchat.ui.view.adapters.StoriesAdapter
import com.vijaydhoni.quickchat.ui.viewmodel.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import com.vijaydhoni.quickchat.util.getTimeInFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.util.*

@AndroidEntryPoint
class StoriesFragment : Fragment() {
    private lateinit var binding: FragmentStoriesBinding
    private var imageUri: Uri? = null
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>
    private val chatViewModel: ChatViewModel by activityViewModels()
    private var currenUserId: String? = null
    private lateinit var storieRv: StoriesAdapter
    private var activeUserStory: UserStory? = null
    private var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        imageUri = uri
                        chatViewModel.saveUserStatus(imageUri!!)
                    }
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoriesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId()
        setRv()
        chatViewModel.getCurrentUserStory()
        chatViewModel.getUserStories()
        addStoryClicked()
        observeSaveStory()
        observeStories()
        storiesRvItemOnCLick()
        observeCurrentUserStory()
        storySeenUi(activeUserStory)
    }

    private fun storySeenUi(userStory: UserStory?) {
        if (userStory != null) {
            for (i in 0 until userStory.stories.size) {
                val status: Story = userStory.stories[i]
                val color: Int =
                    if (userStory.user?.userId in status.storySeenBy) Color.GRAY else Color.BLUE
                binding.circularStatusView.setPortionColorForIndex(i, color)
            }
        }
    }

    private fun observeCurrentUserStory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.currentUserStory.collectLatest { response ->
                    when (response) {

                        is Resource.Success -> {
                            val currentUserStory = response.data
                            if (currentUserStory != null && !currentUserStory.stories.isNullOrEmpty()) {
                                changeUiForShowingCurrentUserStory(currentUserStory)
                                activeUserStory = currentUserStory
                            } else {
                                changeUiforHidingCurrentUserStoryUi()
                            }
                        }

                        is Resource.Error -> {
                            changeUiforHidingCurrentUserStoryUi()
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {}

                    }
                }
            }
        }
    }

    private fun changeUiforHidingCurrentUserStoryUi() {
        binding.userStatusProfile.visibility = View.VISIBLE
        binding.addStatusBttn.visibility = View.VISIBLE
        binding.withoutStatusAppliedLinearLay.visibility = View.VISIBLE
        binding.usrStatus.visibility = View.GONE
        binding.circularStatusView.visibility = View.GONE
        binding.linearLayAfterStatusApplied.visibility = View.GONE
        binding.storyAddOptions.visibility = View.GONE
    }

    private fun changeUiForShowingCurrentUserStory(currentUserStory: UserStory) {

        binding.userStatusProfile.visibility = View.GONE
        binding.addStatusBttn.visibility = View.GONE
        binding.withoutStatusAppliedLinearLay.visibility = View.GONE
        binding.usrStatus.visibility = View.VISIBLE
        binding.circularStatusView.visibility = View.VISIBLE
        binding.storyAddOptions.visibility = View.VISIBLE
        binding.linearLayAfterStatusApplied.visibility = View.VISIBLE
        val lastStatusIndex = currentUserStory.stories.size - 1
        if (lastStatusIndex >= 0) {
            val lastStatus = currentUserStory.stories[lastStatusIndex]
            Glide.with(binding.usrStatus)
                .load(lastStatus.storyImageUrl)
                .into(binding.usrStatus)
        }

        binding.circularStatusView.setPortionsCount(currentUserStory.stories.size)
        binding.currentUserStatusTime.text = getTimeInFormat(currentUserStory.lastUpdatedTime!!)
        for (i in 0 until currentUserStory.stories.size) {
            val status: Story = currentUserStory.stories[i]
            val color: Int =
                if (currenUserId in status.storySeenBy) Color.GRAY else Color.BLUE
            binding.circularStatusView.setPortionColorForIndex(i, color)
        }

    }


    private fun currentUserId() {
        currenUserId = chatViewModel.getCurrentUserId()
    }

    private fun storiesRvItemOnCLick() {
        storieRv.onClick = { userStory ->
            setStoriesView(userStory)
        }
    }

    private fun setStoriesView(userStory: UserStory) {
        val myStories = ArrayList<MyStory>()
        for (story in userStory.stories) {
            myStories.add(MyStory(story.storyImageUrl))
        }
        StoryView.Builder(requireActivity().supportFragmentManager)
            .setStoriesList(myStories)
            .setStoryDuration(8000).setTitleText(userStory.user?.userName)
            .setSubtitleText("last updated ${getTimeInFormat(userStory.lastUpdatedTime!!)}")
            .setOnStoryChangedCallback {
                val currentStory = userStory.stories[it]
                chatViewModel.setUserStorySeen(currentStory)
            }.setTitleLogoUrl(userStory.user?.imagePath)
            .setStoryClickListeners(object : StoryClickListeners {
                override fun onDescriptionClickListener(position: Int) {}
                override fun onTitleIconClickListener(position: Int) {
//                        val user = userStory.user
//                        user?.let {
//                            val bundle = Bundle()
//                            bundle.putParcelable("user", it)
//                            findNavController().navigate(
//                                R.id.action_stories_fragment_to_chatFragment,
//                                bundle
//                            )
//                        }

                }

            })
            .build()
            .show()
    }


    private fun observeStories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.userStories.collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val usersStories = resource.data
                            if (!usersStories.isNullOrEmpty()) {
                                storieRv.differ.submitList(usersStories)
                            } else {
                                Log.d("stories", "no story found")
                            }
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {}

                    }
                }
            }
        }
    }

    private fun setRv() {
        storieRv = StoriesAdapter(currenUserId,requireContext())
        binding.recentStoriesRv.apply {
            adapter = storieRv
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(VerticalItemdecorationRv(20))
        }
    }

    private fun observeSaveStory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.saveUserStory.collectLatest { response ->
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

    private fun addStoryClicked() {
        binding.addStatusBttn.setOnClickListener {
            openImagePicker()
        }

        binding.tapToAddStatus.setOnClickListener {
            openImagePicker()
        }

        binding.usrStatus.setOnClickListener {
            activeUserStory?.let {
                openMyStatus(it)
            }
        }

        binding.linearLayAfterStatusApplied.setOnClickListener {
            activeUserStory?.let {
                openMyStatus(it)
            }
        }

        binding.storyAddOptions.setOnClickListener {
            val popupMenu = PopupMenu(
                context,
                binding.storyAddOptions
            )
            popupMenu.inflate(R.menu.add_story_item_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.add_story -> {
                        openImagePicker()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }
    }

    private fun openMyStatus(it: UserStory) {
        val bundle = Bundle()
        bundle.putParcelable("myStorys", it)
        findNavController().navigate(
            R.id.action_stories_fragment_to_myStatusFragment,
            bundle
        )
    }

    private fun openImagePicker() {
        ImagePicker.with(requireActivity())
            .createIntentFromDialog {
                imageLauncher.launch(it)
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

    override fun onResume() {
        super.onResume()
        val activity = activity as? ChatActivity
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)?.let { bnv ->
            bnv.visibility = View.VISIBLE
        }
    }

}