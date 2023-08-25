package com.aksapps.chirpchat.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.Models.User
import com.aksapps.chirpchat.databinding.ActivitySetupProfileBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextInputEditText
    private lateinit var createProfileBtn: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri? = null
    private lateinit var progressLayout: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        profileImage = binding.imgProfileImage
        userName = binding.etUserName
        createProfileBtn = binding.btnCreateProfile
        progressLayout = AlertDialog.Builder(this@SetupProfileActivity)
        progressLayout.setView(R.layout.progress_layout)
        progressLayout.setCancelable(false)
        alertDialog = progressLayout.create()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
        createProfileBtn.setOnClickListener {
            val name: String = userName.text.toString()
            if (name.isEmpty()) {
                userName.error = "Please type a name."
                return@setOnClickListener
            }
            if (selectedImage != null) {
                alertDialog.show()
                val reference: StorageReference =
                    storage.reference.child("Profiles").child(auth.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val uid = auth.uid
                            val phone = auth.currentUser?.phoneNumber
                            //val name = userName.text.toString()
                            val user: User = User(uid.toString(), name, phone.toString(), imageUrl)

                            database.reference.child("Users")
                                .child(uid.toString())
                                .setValue(user)
                                .addOnSuccessListener {
                                    alertDialog.dismiss()
                                    val intent =
                                        Intent(this@SetupProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                }
            } else {
                val uid = auth.uid
                val phone = auth.currentUser?.phoneNumber
                //val name = userName.text.toString()
                val user: User = User(uid.toString(), name, phone.toString(), "No Image")

                database.reference.child("Users")
                    .child(uid.toString())
                    .setValue(user)
                    .addOnSuccessListener {
                        alertDialog.dismiss()
                        val intent =
                            Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result
                if (data != null) {
                    if (data.data != null) {
                        profileImage.setImageURI(data.data)
                        selectedImage = data.data
                    }
                }
            }
        }

}