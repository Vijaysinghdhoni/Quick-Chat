package com.vijaydhoni.quickchat.ui.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R.*
import com.vijaydhoni.quickchat.data.models.ChatRoom
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.RecentsChatsItemBinding
import java.text.SimpleDateFormat
import java.util.*

class RecentChatRvAdapter(private val currentUserId: String?, private val context: Context) :
    RecyclerView.Adapter<RecentChatRvAdapter.RecentsChatViewHolder>() {

    private val callBack = object : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.chatRoomId == newItem.chatRoomId
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, callBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentsChatViewHolder {
        return RecentsChatViewHolder(
            RecentsChatsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RecentsChatViewHolder, position: Int) {
        val chatRoom = differ.currentList[position]
        val userIds = chatRoom.usersIds

        userData?.let {
            it(userIds) {
                holder.bind(chatRoom, currentUserId, it, context)
            }
        }


    }

    var userData: ((List<String?>, (User?) -> Unit) -> Unit)? = null


    var onItemClick: ((User) -> Unit)? = null

    inner class RecentsChatViewHolder(private val binding: RecentsChatsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(chatRoom: ChatRoom, currentUserId: String?, user: User?, context: Context) {
            if (chatRoom.usersIds[0] == currentUserId && chatRoom.usersIds[1] == currentUserId) {
                binding.recentChatLinearLay.visibility = View.GONE
                binding.usrProfilePic.visibility = View.GONE
                binding.timeLay.visibility = View.GONE
            } else {
                binding.recentChatLinearLay.visibility = View.VISIBLE
                binding.usrProfilePic.visibility = View.VISIBLE
                binding.timeLay.visibility = View.VISIBLE
                if (chatRoom.lastMssgSenderId != currentUserId && !chatRoom.lastMssgSeen) {
                    binding.lastMsg.setTextColor(ContextCompat.getColor(context, color.g_blue))
                    binding.lastMsg.text = chatRoom.lastMssg
                    binding.lastMsg.textSize = 16f
                    binding.msgNotSeenBackground.visibility = View.VISIBLE
                } else {
                    binding.lastMsg.setTextColor(
                        ContextCompat.getColor(
                            context,
                            color.g_light_black
                        )
                    )
                    binding.lastMsg.text = "you : ${chatRoom.lastMssg}"
                    binding.lastMsg.textSize = 12f
                    binding.msgNotSeenBackground.visibility = View.GONE
                }

                binding.usrNameTxt.text = user?.userName
                binding.root.setOnClickListener {
                    onItemClick?.invoke(user!!)
                }
                binding.lastMsgTime.text = getTimeInFormat(chatRoom.lastMssgTimeStamp!!)

                if (user?.imagePath?.isNotEmpty() == true) {
                    Glide.with(binding.usrProfilePic)
                        .load(user.imagePath)
                        .placeholder(drawable.person_profile_place_holder)
                        .error(drawable.person_profile_place_holder)
                        .into(binding.usrProfilePic)
                }


            }

        }

        private fun getTimeInFormat(timeStamp: Timestamp): String {
            val currentDate = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply {
                time = timeStamp.toDate()
            }

            return if (currentDate.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
                && currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
            ) {
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(timeStamp.toDate())
            } else {
                SimpleDateFormat("MMM dd", Locale.ENGLISH).format(timeStamp.toDate())
            }
        }

    }
}