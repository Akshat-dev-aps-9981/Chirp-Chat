package com.aksapps.chirpchat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aksapps.chirpchat.Models.Message
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.databinding.ItemRecievedBinding
import com.aksapps.chirpchat.databinding.ItemSentBinding
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MessageAdapter(
    private val context: Context,
    private val messages: ArrayList<Message>,
    private val senderRoom: String,
    private val recieverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SENT_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_sent, parent, false)
                SentViewHolder(view)
            }
            RECEIVED_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recieved, parent, false)
                ReceivedViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (FirebaseAuth.getInstance().uid.equals(message.senderId)) {
            return SENT_VIEW_TYPE
        } else {
            return RECEIVED_VIEW_TYPE
        }
        return super.getItemViewType(position)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]
/*        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder
        }*/

        val reactions = intArrayOf(
            R.drawable.like,
            R.drawable.heart,
            R.drawable.laughing,
            R.drawable.wow,
            R.drawable.sad,
            R.drawable.angry
        )

        val config = ReactionsConfigBuilder(context)
            .withReactions(reactions)
            .build()

        val popup = ReactionPopup(context, config, { pos: Int? ->
            if (holder.itemViewType == SENT_VIEW_TYPE) {
                val sentHolder = holder as SentViewHolder
                if (pos != null && pos != -1) {
                    sentHolder.binding.imgReactionSent.setImageResource(reactions[pos])
                    sentHolder.binding.imgReactionSent.visibility = View.VISIBLE
                }
            } else {
                val recieveHolder = holder as ReceivedViewHolder
                if (pos != null && pos != -1) {
                    recieveHolder.binding.imgReactionRecieved.setImageResource(reactions[pos])
                    recieveHolder.binding.imgReactionRecieved.visibility = View.VISIBLE
                }
            }

            if (pos != null) {
                message.feeling = pos
            }

            message.messageId?.let {
                FirebaseDatabase.getInstance().reference
                    .child("Chats")
                    .child(senderRoom)
                    .child("Messages")
                    .child(it).setValue(message)
            }

            message.messageId?.let {
                FirebaseDatabase.getInstance().reference
                    .child("Chats")
                    .child(recieverRoom)
                    .child("Messages")
                    .child(it).setValue(message)
            }

            true // true is closing popup, false is requesting a new selection
        })

        when (holder.itemViewType) {
            SENT_VIEW_TYPE -> {
                val sentHolder = holder as SentViewHolder
                if (message.message.equals("Photo") && message.imageUrl != null) {
                    sentHolder.binding.sendImage.visibility = View.VISIBLE
                    sentHolder.binding.txtSentText.visibility = View.GONE
                    Glide.with(context).load(message.imageUrl).placeholder(R.drawable.ic_profile).error(R.drawable.wow).into(sentHolder.binding.sendImage)
                }
                sentHolder.binding.txtSentText.text = message.message

                if (message.feeling!! >= 0) {
//                    message.feeling = reactions[message.feeling]
                    sentHolder.binding.imgReactionSent.setImageResource(reactions[message.feeling!!])
                    sentHolder.binding.imgReactionSent.visibility = View.VISIBLE
                } else {
                    sentHolder.binding.imgReactionSent.visibility = View.GONE
                }

                sentHolder.binding.txtSentText.setOnTouchListener { view, motionEvent ->
                    popup.onTouch(view, motionEvent)
                }

                sentHolder.binding.sendImage.setOnTouchListener { view, motionEvent ->
                    popup.onTouch(view, motionEvent)
                }

                /*sentHolder.binding.txtSentText.setOnTouchListener(object : View.OnTouchListener {
                    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
                        if (motionEvent != null) {
                            if (v != null) {
                                popup.onTouch(v, motionEvent)
                            }
                        }
                        return false
                    }
                })*/

                val longTimestamp: Long? = message.timestamp // Example long timestamp

                val date = longTimestamp?.let { Date(it) }
                val normalTimestamp = date.toString()

                sentHolder.binding.txtSentTimeStamp.text = normalTimestamp
                // Bind data to the SentViewHolder's views
            }
            RECEIVED_VIEW_TYPE -> {
                val receivedHolder = holder as ReceivedViewHolder

                if (message.message.equals("Photo") && message.imageUrl != null) {
                    receivedHolder.binding.receivedImage.visibility = View.VISIBLE
                    receivedHolder.binding.txtRecievedText.visibility = View.GONE
                    Glide.with(context).load(message.imageUrl).placeholder(R.drawable.ic_profile).error(R.drawable.wow).into(receivedHolder.binding.receivedImage)
                }

                receivedHolder.binding.txtRecievedText.text = message.message

                if (message.feeling!! >= 0) {
//                    message.feeling = reactions[message.feeling]
                    receivedHolder.binding.imgReactionRecieved.setImageResource(reactions[message.feeling!!])
                    receivedHolder.binding.imgReactionRecieved.visibility = View.VISIBLE
                } else {
                    receivedHolder.binding.imgReactionRecieved.visibility = View.GONE
                }

                receivedHolder.binding.txtRecievedText.setOnTouchListener { view, motionEvent ->
                    popup.onTouch(view, motionEvent)
                }

                receivedHolder.binding.receivedImage.setOnTouchListener { view, motionEvent ->
                    popup.onTouch(view, motionEvent)
                }

                val longTimestamp: Long? = message.timestamp // Example long timestamp

                val date = longTimestamp?.let { Date(it) }
                val normalTimestamp = date.toString()
                receivedHolder.binding.txtRecievedTimeStamp.text = normalTimestamp
                // Bind data to the ReceivedViewHolder's views
            }
        }
    }

    override fun getItemCount(): Int {
        // Implement your getItemCount logic here
        return messages.size
    }

    companion object {
        private const val SENT_VIEW_TYPE = 1
        private const val RECEIVED_VIEW_TYPE = 2
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemSentBinding = ItemSentBinding.bind(itemView)
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecievedBinding = ItemRecievedBinding.bind(itemView)
    }

}