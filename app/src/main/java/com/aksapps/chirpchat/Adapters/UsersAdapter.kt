package com.aksapps.chirpchat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aksapps.chirpchat.Activities.ChatActivity
import com.aksapps.chirpchat.Models.User
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.databinding.RowConversationBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UsersAdapter(private val context: Context, private val users: ArrayList<User>) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user: User = users[position]

        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user.uid

        FirebaseDatabase.getInstance().reference
            .child("Chats")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child("lastMsg").getValue(String::class.java)
                        val time = snapshot.child("lastMsgTime").getValue(Long::class.java)

                        holder.binding.txtLastMessage.text = lastMsg
                        holder.binding.txtLastMessageTimestamp.text = time?.let {
                            getTimeFromTimestamp(
                                it
                            )
                        }
                    } else {
                        holder.binding.txtLastMessage.text = "Tap to Start a Chat!"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        holder.binding.txtUserName.text = user.name
        holder.binding.txtUnreadCounter.visibility = View.VISIBLE
        Glide.with(context).load(user.profileImage).error(R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile).into(holder.binding.imgUserProfile)
        holder.itemView.setOnClickListener {
            holder.binding.txtUnreadCounter.visibility = View.GONE
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    fun getTimeFromTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowConversationBinding = RowConversationBinding.bind(itemView)
    }
}