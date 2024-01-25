package com.vijaydhoni.quickchat.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vijaydhoni.quickchat.data.models.Intro
import com.vijaydhoni.quickchat.databinding.IntroviewpageritemlayoutBinding

class IntroViewPagerAdapter :
    RecyclerView.Adapter<IntroViewPagerAdapter.IntroViewpagerViewholder>() {


    private val callback = object : DiffUtil.ItemCallback<Intro>() {
        override fun areItemsTheSame(oldItem: Intro, newItem: Intro): Boolean {
            return oldItem.headLineTxt == newItem.headLineTxt
        }

        override fun areContentsTheSame(oldItem: Intro, newItem: Intro): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, callback)

    inner class IntroViewpagerViewholder(val binding: IntroviewpageritemlayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(intro: Intro) {

            binding.introImg.setImageResource(intro.introImg)
            binding.introHeadlineTxt.text = intro.headLineTxt
            binding.introSubHead.text = intro.subheadTxt

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewpagerViewholder {
        return IntroViewpagerViewholder(
            IntroviewpageritemlayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: IntroViewpagerViewholder, position: Int) {
        val intro = differ.currentList[position]
        holder.bind(intro)
    }
}