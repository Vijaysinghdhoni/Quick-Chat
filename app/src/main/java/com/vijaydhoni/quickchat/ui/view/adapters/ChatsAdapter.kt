package com.vijaydhoni.quickchat.ui.view.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Message
import com.vijaydhoni.quickchat.databinding.ChatRvMssgItemBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(private val currentUserId: String) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewholder>() {

    private val callback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.senderId == newItem.senderId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewholder {
        return ChatsViewholder(
            ChatRvMssgItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ChatsViewholder, position: Int) {
        val message = differ.currentList[position]
        holder.bind(currentUserId, message)
    }


    inner class ChatsViewholder(private val binding: ChatRvMssgItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(currentUserId: String, message: Message) {

            if (message.senderId.equals(currentUserId)) {
                binding.reciverLayout.visibility = View.GONE
                binding.senderLayout.visibility = View.VISIBLE
                binding.senderMssg.text = message.message
                binding.seenLay.visibility = View.VISIBLE
                binding.messageTime.visibility = View.VISIBLE
                if (message.seen) {
                    val imageDrawable =
                        ColorDrawable(R.color.g_blue)
                    binding.msgSeenCiricleBack.setImageDrawable(imageDrawable)
                }
                binding.messageTime.text = getTimeInFormat(message.timeStamp)

            } else {
                binding.reciverLayout.visibility = View.VISIBLE
                binding.senderLayout.visibility = View.GONE
                binding.reciverMsg.text = message.message
                binding.seenLay.visibility = View.GONE
                binding.messageTime.visibility = View.GONE
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