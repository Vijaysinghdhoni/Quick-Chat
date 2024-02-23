package com.vijaydhoni.quickchat.ui.view.adapters

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
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.Story
import com.vijaydhoni.quickchat.data.models.UserStory
import com.vijaydhoni.quickchat.databinding.StorieRvItemBinding
import java.text.SimpleDateFormat
import java.util.*

class StoriesAdapter(private val currenUserId: String?, private val context: Context) :
    RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<UserStory>() {
        override fun areItemsTheSame(oldItem: UserStory, newItem: UserStory): Boolean {
            return oldItem.user == newItem.user
        }

        override fun areContentsTheSame(oldItem: UserStory, newItem: UserStory): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        return StoriesViewHolder(
            StorieRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val userStory = differ.currentList[position]
        holder.bind(userStory, currenUserId, context)
    }

    var onClick: ((UserStory) -> Unit)? = null

    inner class StoriesViewHolder(private val binding: StorieRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userStory: UserStory, currenUserId: String?, context: Context) {
            if (userStory.user?.userId != currenUserId) {

                binding.circularStatusView.visibility = View.VISIBLE
                binding.myStatusImage.visibility = View.VISIBLE
                binding.otherUsrName.visibility = View.VISIBLE
                binding.statusTime.visibility = View.VISIBLE

                binding.otherUsrName.text = userStory.user?.userName
                binding.statusTime.text = getTimeInFormat(userStory.lastUpdatedTime!!)
                val lastStatusIndex = userStory.stories.size - 1
                if (lastStatusIndex >= 0) {
                    val lastStatus = userStory.stories[lastStatusIndex]
                    Glide.with(binding.myStatusImage)
                        .load(lastStatus.storyImageUrl)
                        .placeholder(R.drawable.person_profile_place_holder)
                        .error(R.drawable.person_profile_place_holder)
                        .into(binding.myStatusImage)
                }
                binding.circularStatusView.setPortionsCount(userStory.stories.size)
                binding.myStatusImage.setOnClickListener {
                    onClick?.invoke(userStory)
                }
                binding.root.setOnClickListener {
                    onClick?.invoke(userStory)
                }

                for (i in 0 until userStory.stories.size) {
                    val status: Story = userStory.stories[i]
                    val color: Int =
                        if (currenUserId in status.storySeenBy) ContextCompat.getColor(
                            context,
                            R.color.g_grey
                        ) else ContextCompat.getColor(context, R.color.g_blue)
                    binding.circularStatusView.setPortionColorForIndex(i, color)
                }

            } else {
                binding.circularStatusView.visibility = View.GONE
                binding.myStatusImage.visibility = View.GONE
                binding.otherUsrName.visibility = View.GONE
                binding.statusTime.visibility = View.GONE
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