package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.graphics.Rect
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.ChatRoom
import com.vijaydhoni.quickchat.data.models.Message
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.FragmentChatBinding
import com.vijaydhoni.quickchat.ui.view.adapters.ChatsAdapter
import com.vijaydhoni.quickchat.ui.viewmodels.ChatViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.VerticalItemdecorationRv
import com.vijaydhoni.quickchat.util.getChatRoomId
import com.vijaydhoni.quickchat.util.setStatusBarColour
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private val binding: FragmentChatBinding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }
    private lateinit var chatsAdapter: ChatsAdapter
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private var currentUserId: String? = null
    private var id: String? = null
    private var chatIDs: String? = null
    private var newChatRoom: ChatRoom? = null
    private lateinit var otherUser: User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCuurentUserId()
        setUpChatsAdapter()
        val args by navArgs<ChatFragmentArgs>()
        otherUser = args.user
        id = otherUser.userId
        setUserDetials(otherUser)
        getChatRoom()
        observeGetChatId()
        backButtonClick()
        sendMssgBttnCLick()
        observeSendMssg()
        getUserAllMessages()
        observeUserMessages()
        upcomingFeature()
    }


    private fun setUserDetials(otherUser: User) {
        if (otherUser.imagePath.isNotEmpty()) {
            Glide.with(binding.userProfilePic)
                .load(otherUser.imagePath)
                .placeholder(R.drawable.person_profile_place_holder)
                .error(R.color.g_black)
                .into(binding.userProfilePic)
        }
        binding.userName.text = otherUser.userName
        if (otherUser.isUserActive) {
            binding.activeTime.text = "Online"
        } else {
            binding.activeTime.text = getTimeInFormat(otherUser.lastSeenTime)
        }
    }

    private fun getTimeInFormat(timeStamp: Timestamp): String {
        val currentDate = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply {
            time = timeStamp.toDate()
        }
        val minutesAgo = (currentDate.timeInMillis - messageDate.timeInMillis) / (60 * 1000)
        val timeAgo: String
        return if (minutesAgo < 60) {
            "Active $minutesAgo m ago"
        } else if (currentDate.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
            && currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
        ) {
            timeAgo = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(timeStamp.toDate())
            "last seen $timeAgo"
        } else {
            timeAgo = SimpleDateFormat("MMM dd", Locale.ENGLISH).format(timeStamp.toDate())
            "last seen $timeAgo"
        }
    }

//    private fun sendMssgBttnCLick() {
//        binding.sendMssgBttn.setOnClickListener {
//            sendUserMessage()
//        }
//
//        binding.emojiBttn.setOnClickListener {
//            binding.emojiPicker.visibility = View.VISIBLE
//            binding.emojiPicker.setOnEmojiPickedListener {
//                binding.usrMsgEt.append(it.emoji)
//            }
//        }
//
//        binding.usrMsgEt.setOnFocusChangeListener { _, hasfocus ->
//            if (hasfocus) {
//                binding.emojiPicker.visibility = View.GONE
//            }
//        }
//    }

    private fun sendMssgBttnCLick() {
        binding.sendMssgBttn.setOnClickListener {
            sendUserMessage()
        }

        binding.emojiBttn.setOnClickListener {

            binding.emojiPicker.visibility = if (binding.emojiPicker.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.emojiPicker.setOnEmojiPickedListener {
                binding.usrMsgEt.append(it.emoji)
            }
        }

        binding.usrMsgEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.emojiPicker.visibility == View.VISIBLE) {
                    binding.emojiPicker.visibility = View.GONE
                }
            }
        }
    }


    private fun backButtonClick() {
        binding.backBttn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUpChatsAdapter() {
        val manager = LinearLayoutManager(requireContext())
        currentUserId?.let {
            chatsAdapter = ChatsAdapter(it)
            binding.chatsRecylerView.apply {
                adapter = chatsAdapter
                manager.reverseLayout = true
                layoutManager = manager
                addItemDecoration(VerticalItemdecorationRv(6))
            }
        }

        chatsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatsRecylerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun observeUserMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { response ->
                    when (response) {


                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val messages = response.data
                            if (messages != null) {
                                chatsAdapter.differ.submitList(messages)
                            }
                        }

                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                            Log.d("chatroom", response.message.toString())
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun getUserAllMessages() {
        chatIDs?.let {
            chatViewModel.getAllUsersMessages(it)
        }
    }

    private fun observeSendMssg() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messageSend.collectLatest { response ->

                    when (response) {


                        is Resource.Success -> {
                            Log.d("sendMssg", response.data.toString())
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                            Log.d("chatroom", response.message.toString())
                        }

                        else -> {}

                    }

                }
            }
        }
    }

    private fun sendUserMessage() {
        val message = binding.usrMsgEt.text.trim().toString()
        if (message.isEmpty()) {
            return
        }

        newChatRoom?.let {
            it.lastMssgTimeStamp = Timestamp.now()
            it.lastMssgSenderId = currentUserId
            it.lastMssg = message
            it.lastMssgSeen = false
            chatViewModel.setChatRoom(it)
            val messageModule =
                Message(message = message, senderId = currentUserId, Timestamp.now(), seen = false)
            chatViewModel.sendUserMessage(message = messageModule, chatIDs!!, otherUser)
            binding.usrMsgEt.setText("")
            Log.d("sendMssg", "messageSent")
        }

    }

    private fun observeGetChatId() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.chatRoom.collectLatest { response ->
                    when (response) {

                        is Resource.Success -> {
                            val chatRoom = response.data
                            if (chatRoom == null) {
                                newChatRoom = ChatRoom(
                                    chatIDs,
                                    listOf(currentUserId, id),
                                    Timestamp.now()
                                )
                                chatViewModel.setChatRoom(newChatRoom!!)
                                Log.d("usrs", "new chat room is created with usr $otherUser")
                            } else {
                                newChatRoom = chatRoom
                            }
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                            Log.d("chatroom", response.message.toString())
                        }

                        else -> {
                        }

                    }
                }
            }
        }
    }

    private fun getChatRoom() {
        if (id != null && currentUserId != null) {
            chatIDs = getChatRoomId(currentUserId!!, id!!)
            chatViewModel.getChatRoom(chatIDs!!)
        }
    }

    private fun getCuurentUserId() {
        currentUserId = chatViewModel.getCurrentUserId()
    }

    private fun upcomingFeature() {
        binding.cameraBttn.setOnClickListener {
            Toast.makeText(requireContext(), "feature coming soon!!", Toast.LENGTH_SHORT).show()
        }
        binding.attachMediaBttn.setOnClickListener {
            Toast.makeText(requireContext(), "feature coming soon!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("chatroom", "on resume is called")
        val bottomNavigation =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
        bottomNavigation?.visibility = View.GONE
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }

}