package com.example.submission.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.submission.network.StoryResponse
import com.example.submission.databinding.ItemRowStoryBinding
import com.example.submission.view.detailStory.DetailStoryActivity

class StoryListAdapter :
    PagingDataAdapter<StoryResponse, StoryListAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: ItemRowStoryBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(story: StoryResponse){

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.itemPhoto)

            binding.itemUsername.text = story.name
            binding.itemDescription.text = story.description

            itemView.setOnClickListener {

                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                intent.putExtra("Story", story)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.itemPhoto, "picture"),
                        Pair(binding.itemUsername, "username"),
                        Pair(binding.itemDescription, "description")
                    )

                itemView.context.startActivity(intent, optionsCompat.toBundle())

            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryResponse>() {
            override fun areItemsTheSame(oldItem: StoryResponse, newItem: StoryResponse): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryResponse, newItem: StoryResponse): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

}