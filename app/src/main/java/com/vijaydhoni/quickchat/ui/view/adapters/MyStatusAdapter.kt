package com.vijaydhoni.quickchat.ui.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Story
import com.vijaydhoni.quickchat.databinding.MyStatusItemBinding
import java.text.SimpleDateFormat
import java.util.*

class MyStatusAdapter(private val currentUserId: String, private val context: Context) :
    RecyclerView.Adapter<MyStatusAdapter.MyStatusViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.userID == newItem.userID
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStatusViewHolder {
        return MyStatusViewHolder(
            MyStatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MyStatusViewHolder, position: Int) {
        val story = differ.currentList[position]
        holder.bind(story, currentUserId, context)
    }

    var onStatusClick: ((Story) -> Unit)? = null
    var onDeleteClick: ((Story) -> Unit)? = null

    inner class MyStatusViewHolder(private val binding: MyStatusItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story, currentUserId: String, context: Context) {
            if (currentUserId !in story.storySeenBy) {
                binding.storieViews.text = "${story.storySeenBy.size} views"
            } else {
                binding.storieViews.text = "${(story.storySeenBy.size - 1)} views "
            }

            Glide.with(binding.myStatusImage)
                .load(story.storyImageUrl)
                .placeholder(R.drawable.person_profile_place_holder)
                .error(R.drawable.person_profile_place_holder)
                .into(binding.myStatusImage)

            binding.statusTime.text = getTimeInFormat(story.storyCreatedTimestamp!!)
            binding.storyDeleteOptions.setOnClickListener {
                val popupMenu = PopupMenu(
                    context,
                    binding.storyDeleteOptions
                )
                popupMenu.inflate(R.menu.my_status_rv_item_menu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_story -> {
                            onDeleteClick?.invoke(story)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
                popupMenu.show()
            }
            binding.myStatusImage.setOnClickListener {
                onStatusClick?.invoke(story)
            }
            binding.myStatusLinearLay.setOnClickListener {
                onStatusClick?.invoke(story)
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