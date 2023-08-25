package com.aksapps.chirpchat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.aksapps.chirpchat.Activities.MainActivity
import com.aksapps.chirpchat.Models.Status
import com.aksapps.chirpchat.Models.UserStatus
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.databinding.ItemStatusBinding
import com.bumptech.glide.Glide
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.lang.Exception

class TopStatusAdapter(val context: Context, val userStatuses: ArrayList<UserStatus>) : RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_status, parent,false)
        return TopStatusViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userStatuses.size - 1
    }

    override fun onBindViewHolder(holder: TopStatusViewHolder, position: Int) {
        val userStatus: UserStatus = userStatuses[position]
        try {
            val lastStatus: Status? = userStatus.statuses?.get(userStatus.statuses?.size?.minus(1) ?: 0)
            Glide.with(context).load(lastStatus?.imageUrl).placeholder(R.mipmap.ic_launcher).error(R.drawable.ic_profile).into(holder.binding.image1)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }

        holder.binding.circularStatusView.setPortionsCount(userStatus.statuses?.size!!)
        holder.binding.circularStatusView.setOnClickListener {
            val myStories : ArrayList<MyStory> = arrayListOf()
            for (status: Status in userStatus.statuses!!) {
                myStories.add(MyStory(status.imageUrl))
            }

            val fragmentManager = (context as MainActivity).supportFragmentManager

            val storyView = StoryView.Builder(fragmentManager)
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000L) // Default is 2000 Millis (2 Seconds)
                .setTitleText(userStatus.name) // Default is Hidden
                .setSubtitleText("Chirp-Chat") // Default is Hidden
                .setTitleLogoUrl(userStatus.profileImage) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        // Your action
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        // Your action
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method

            storyView.show()

        }
    }

    class TopStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemStatusBinding.bind(itemView)
    }
}