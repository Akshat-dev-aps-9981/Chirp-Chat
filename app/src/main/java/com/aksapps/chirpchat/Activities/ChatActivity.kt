package com.aksapps.chirpchat.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aksapps.chirpchat.Adapters.MessageAdapter
import com.aksapps.chirpchat.Models.Message
import com.aksapps.chirpchat.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {
    // Declaration.
    private lateinit var binding: ActivityChatBinding
    private lateinit var messages: ArrayList<Message>
    private lateinit var adapter: MessageAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var senderUid: String
    private lateinit var recieverUid: String

    private lateinit var senderRoom: String
    private lateinit var recieverRoom: String

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Getting values from intent of previous activity (MainActivity).
        val name = intent.getStringExtra("name").toString()
        recieverUid = intent.getStringExtra("uid").toString()

        senderUid = FirebaseAuth.getInstance().uid.toString()

        setSupportActionBar(binding.toolbar)

        // Creating rooms to send and receive messages.
        senderRoom = senderUid + recieverUid
        recieverRoom = recieverUid + senderUid

        // Initializing firebase database & storage.
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initializing message and adapter.
        messages = arrayListOf<Message>()
        adapter = MessageAdapter(this as Context, messages, senderRoom, recieverRoom)

        // Initializing progress bar.
        progressDialog = ProgressDialog(this as Context)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Uploading Image...")
        progressDialog.setCancelable(false)

        // Setting layout manager and adapter for chats.
        binding.chatRecycler.layoutManager = LinearLayoutManager(this as Context)
        binding.chatRecycler.adapter = adapter

        // Adding back button on toolbar.
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.name.text = name
       /* supportActionBar?.title = name
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)*/

        // Functionality of clicking on camera button next to chat edit box and before send button.
        binding.imgCamera.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }

        // This code will load chats.
        database.reference.child("Chats")
            .child(senderRoom)
            .child("Messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(Message::class.java)
                        if (message != null) {
                            message.messageId = snapshot1.key
                            val message2 = Message(snapshot1.key.toString())
                            message2.messageId = snapshot1.key.toString()
                            /* Log.w("Message: ", snapshot1.key.toString())
                             Log.w("Message: ", message.messageId!!)*/
                            messages.add(message)
                        }
                    }
                    // This code will scroll the chat recycler to show latest message.
                    if (messages.isNotEmpty()) {
                        val lastPosition = adapter.itemCount - 1
                        binding.chatRecycler.layoutManager?.smoothScrollToPosition(
                            binding.chatRecycler,
                            null,
                            lastPosition
                        )
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Database error occurred! Error: ${error.message}.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })

        // This code will take care of chats to be scrolled up when soft keyboard is open!
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            if (heightDiff > dpToPx(this, 200)) { // 200 is a threshold value for keyboard height
                // Keyboard is shown, scroll to the last item of the list
                binding.chatRecycler.postDelayed({
                    binding.chatRecycler.scrollToPosition(adapter.itemCount - 1)
                }, 100)
            }
        }

        // Functionality of send button.
        binding.imgSendBtn.setOnClickListener {

            val messageText = binding.etMessageBox.text.toString()

            val date = Date()
            val message = Message(
                message = messageText,
                senderId = senderUid,
                timestamp = date.time,
                feeling = -1
            )
            binding.etMessageBox.setText("")

            val randomKey = database.reference.push().key

            val lastMsgObj = hashMapOf<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time
            database.reference.child("Chats").child(senderRoom).updateChildren(lastMsgObj)
            database.reference.child("Chats").child(recieverRoom).updateChildren(lastMsgObj)

            val messageIdObj = hashMapOf<String, Any>()
            messageIdObj["messageId"] = randomKey.toString()

            if (randomKey != null) {

                database.reference.child("Chats")
                    .child(senderRoom)
                    .child("Messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener {
                        database.reference.child("Chats")
                            .child(recieverRoom)
                            .child("Messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener {
                                database.reference.child("Chats")
                                    .child(senderRoom)
                                    .child("Messages")
                                    .child(randomKey)
                                    .updateChildren(messageIdObj).addOnSuccessListener {
                                        database.reference.child("Chats")
                                            .child(recieverRoom)
                                            .child("Messages")
                                            .child(randomKey)
                                            .updateChildren(messageIdObj).addOnSuccessListener {

                                            }
                                    }
                            }
                    }

            }
        }
    }

    // Handling result for image selected by user on chat.
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result
                if (data != null) {
                    if (data.data != null) {
                        progressDialog.show()
                        val selectedImage: Uri = data.data!!
                        val calendar: Calendar = Calendar.getInstance()
                        val reference: StorageReference = storage.reference.child("Chats").child(calendar.timeInMillis.toString())
                        reference.putFile(selectedImage).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    progressDialog.dismiss()
                                    val filePath: String = uri.toString()

                                    val messageText = binding.etMessageBox.text.toString()

                                    val date = Date()
                                    val message = Message(
                                        message = messageText,
                                        senderId = senderUid,
                                        timestamp = date.time,
                                        feeling = -1
                                    )
                                    message.message = "Photo"
                                    message.imageUrl = filePath

                                    binding.etMessageBox.setText("")

                                    val randomKey = database.reference.push().key

                                    val lastMsgObj = hashMapOf<String, Any>()
                                    lastMsgObj["lastMsg"] = message.message!!
                                    lastMsgObj["lastMsgTime"] = date.time
                                    database.reference.child("Chats").child(senderRoom).updateChildren(lastMsgObj)
                                    database.reference.child("Chats").child(recieverRoom).updateChildren(lastMsgObj)

                                    val messageIdObj = hashMapOf<String, Any>()
                                    messageIdObj["messageId"] = randomKey.toString()

                                    if (randomKey != null) {

                                        database.reference.child("Chats")
                                            .child(senderRoom)
                                            .child("Messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener {
                                                database.reference.child("Chats")
                                                    .child(recieverRoom)
                                                    .child("Messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener {
                                                        database.reference.child("Chats")
                                                            .child(senderRoom)
                                                            .child("Messages")
                                                            .child(randomKey)
                                                            .updateChildren(messageIdObj).addOnSuccessListener {
                                                                database.reference.child("Chats")
                                                                    .child(recieverRoom)
                                                                    .child("Messages")
                                                                    .child(randomKey)
                                                                    .updateChildren(messageIdObj).addOnSuccessListener {

                                                                    }
                                                            }
                                                    }
                                            }

                                    }

                                }
                            }
                        }

                    }
                }
            }
        }

    // To convert dp to pixels.
    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    // For functionality of back button on upper left corner in toolbar.
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}