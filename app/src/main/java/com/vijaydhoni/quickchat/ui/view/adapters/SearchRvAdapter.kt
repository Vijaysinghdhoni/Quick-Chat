package com.vijaydhoni.quickchat.ui.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.SearchRvTemBinding

class SearchRvAdapter(private val currentUserId: String?) :
    RecyclerView.Adapter<SearchRvAdapter.SearchRvViewHolder>() {


    private val callback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.phone == newItem.phone
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, callback)

    inner class SearchRvViewHolder(private val binding: SearchRvTemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(user: User, currentUserId: String?) {

            binding.usrNameTxt.text = user.userName
            binding.usrPhoneNum.text = user.phone

            if (!currentUserId.isNullOrEmpty() && user.userId == currentUserId) {
                binding.usrNameTxt.text = user.userName + "(me)"
            }

            if (user.imagePath.isNotEmpty()) {
                Glide.with(binding.usrProfilePic)
                    .load(user.imagePath)
                    .placeholder(R.drawable.person_profile_place_holder)
                    .error(R.drawable.person_profile_place_holder)
                    .into(binding.usrProfilePic)
            }


        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRvViewHolder {
        return SearchRvViewHolder(
            SearchRvTemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((User) -> Unit)? = null

    override fun onBindViewHolder(holder: SearchRvViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.bind(user, currentUserId)
        holder.itemView.setOnClickListener {
           onClick?.invoke(user)
        }
    }
}