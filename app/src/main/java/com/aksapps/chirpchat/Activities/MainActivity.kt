package com.aksapps.chirpchat.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aksapps.chirpchat.Adapters.TopStatusAdapter
import com.aksapps.chirpchat.Adapters.UsersAdapter
import com.aksapps.chirpchat.Models.Status
import com.aksapps.chirpchat.Models.User
import com.aksapps.chirpchat.Models.UserStatus
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var users: ArrayList<User>
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var topStatusAdapter: TopStatusAdapter
    private lateinit var userStatuses: ArrayList<UserStatus>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var user: User

    private var unreadCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.showShimmerAdapter()
        binding.statusRecycler.showShimmerAdapter()

        database = FirebaseDatabase.getInstance()
        users = arrayListOf<User>()
        userStatuses = arrayListOf<UserStatus>()
        usersAdapter = UsersAdapter(this as Context, users)
        topStatusAdapter = TopStatusAdapter(this as Context, userStatuses)
        binding.recyclerView.layoutManager = LinearLayoutManager(this as Context)
        binding.recyclerView.adapter = usersAdapter

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setMessage("Uploading Status...")
        progressDialog.setCancelable(false)


        FirebaseAuth.getInstance().uid?.let {
            database.reference.child("Users").child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // if (snapshot.exists() && snapshot.getValue(User::class.java) != null) {
                        user = snapshot.getValue(User::class.java)!!
                    /* Log.w("User", "Found!")
                        Log.w("User", user.name.toString())
                        Log.w("User", user.profileImage.toString()) */
                        // }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }


        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.statusRecycler.layoutManager = layoutManager
        binding.statusRecycler.adapter = topStatusAdapter

        database.reference.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    } else {
                        Toast.makeText(this@MainActivity, "No Users found!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                binding.recyclerView.hideShimmerAdapter()
                usersAdapter.notifyDataSetChanged()
            /*Toast.makeText(this@MainActivity, users.toString(), Toast.LENGTH_LONG).show()
                    Log.w("Users : ", users.toString())*/
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        database.reference.child("Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear()
                    for (statusSnapshot: DataSnapshot in snapshot.children) {
                        val status = UserStatus()
                        status.name =
                            statusSnapshot.child("name").getValue(String::class.java).toString()
                        status.profileImage =
                            statusSnapshot.child("profileImage").getValue(String::class.java)
                                .toString()
                        status.lastUpdated =
                            statusSnapshot.child("lastUpdated").getValue(Long::class.java)!!
                        userStatuses.add(status)

                        val statuses: ArrayList<Status> = arrayListOf()
                        for (status2Snapshot: DataSnapshot in statusSnapshot.child("Statuses").children) {
                            val sampleStatus: Status =
                                status2Snapshot.getValue(Status::class.java)!!
                            statuses.add(sampleStatus)
                        }
                        status.statuses = statuses
                        userStatuses.add(status)
                    }
                    binding.statusRecycler.hideShimmerAdapter()
                    topStatusAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Under development.
        /* database.reference.child("Status").addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 if (snapshot.exists()) {
                 userStatuses.clear()
                 for (snapshot1 in snapshot.children) {
                     val status = snapshot1.getValue(Status::class.java)
                     if (status != null) {
                         userStatuses.add(status)
                     } else {
                         Toast.makeText(this@MainActivity, "No Status found!", Toast.LENGTH_SHORT)
                             .show()
                     }
                 }
                 usersAdapter.notifyDataSetChanged()
                 *//*Toast.makeText(this@MainActivity, users.toString(), Toast.LENGTH_LONG).show()
                Log.w("Users : ", users.toString())*//*
            }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })*/

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.status -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    activityResultLauncher.launch(intent)
                }
            }
            false
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    if (data.data != null) {
                        progressDialog.show()
                        val storage = FirebaseStorage.getInstance()
                        val date = Date()
                        val reference =
                            storage.reference.child("Status").child(date.time.toString())
                        reference.putFile(data.data!!).addOnCompleteListener {
                            if (it.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    if (user.name != null && user.profileImage != null) {
                                        val userStatus = UserStatus()
                                        userStatus.name = user.name.toString()
                                        userStatus.profileImage = user.profileImage.toString()
                                        userStatus.lastUpdated = date.time

                                        /*Log.w("Status: ", user.name.toString())
                                        Log.w("Status: ", user.profileImage.toString())*/

                                        val obj: HashMap<String, Any> = hashMapOf()
                                        obj["name"] = userStatus.name
                                        obj["profileImage"] = userStatus.profileImage
                                        obj["lastUpdated"] = userStatus.lastUpdated


                                        val imageUrl = uri.toString()
                                        val status = Status(imageUrl, userStatus.lastUpdated)

                                        FirebaseAuth.getInstance().uid?.let { uid ->
                                            database.reference.child("Status").child(
                                                uid
                                            ).updateChildren(obj)
                                        }

                                        FirebaseAuth.getInstance().uid?.let { it1 ->
                                            database.reference.child("Status").child(it1)
                                                .child("Statuses").push().setValue(status)
                                        }
                                        progressDialog.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                Toast.makeText(this@MainActivity, "Search.", Toast.LENGTH_SHORT).show()
            }

            R.id.groups -> {
                Toast.makeText(this@MainActivity, "Groups.", Toast.LENGTH_SHORT).show()
            }

            R.id.invite -> {
                Toast.makeText(this@MainActivity, "Invite.", Toast.LENGTH_SHORT).show()
            }

            R.id.settings -> {
                Toast.makeText(this@MainActivity, "Settings.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun newMessageReceived() {
        unreadCount++
    }

    fun allMessagesRead() {
        unreadCount = 0
    }
}