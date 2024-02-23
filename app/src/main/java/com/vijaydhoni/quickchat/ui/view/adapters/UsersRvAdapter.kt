package com.vijaydhoni.quickchat.ui.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.UsersRvItemBinding

class UsersRvAdapter(private val currenUserId: String?) :
    RecyclerView.Adapter<UsersRvAdapter.UsersRvViewHolder>() {

    private val callBack = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    val diff = AsyncListDiffer(this, callBack)

    var onClick: ((User) -> Unit)? = null

    inner class UsersRvViewHolder(private val binding: UsersRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, currenUserId: String?) {

            if (currenUserId != user.userId) {
                binding.userImg.visibility = View.VISIBLE
                binding.userName.visibility = View.VISIBLE
                binding.userStatus.visibility = View.VISIBLE
                binding.userName.text = user.userName
                binding.userStatus.text = user.userStatus

                Glide.with(binding.userImg)
                    .load(user.imagePath)
                    .placeholder(R.drawable.person_profile_place_holder)
                    .error(R.drawable.person_profile_place_holder)
                    .into(binding.userImg)

                binding.root.setOnClickListener {
                    onClick?.invoke(user)
                }

            }else{
                binding.userImg.visibility = View.GONE
                binding.userName.visibility = View.GONE
                binding.userStatus.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersRvViewHolder {
        return UsersRvViewHolder(
            UsersRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: UsersRvViewHolder, position: Int) {
        val user = diff.currentList[position]
        holder.bind(user, currenUserId = currenUserId)
    }
}