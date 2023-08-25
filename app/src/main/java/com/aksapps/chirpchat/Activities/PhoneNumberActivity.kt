package com.aksapps.chirpchat.Activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aksapps.chirpchat.databinding.ActivityPhoneNumberBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.rilixtech.widget.countrycodepicker.CountryCodePicker

class PhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneNumberBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var ccp: CountryCodePicker
    private lateinit var btnVerify: MaterialButton
    private lateinit var countryCodeWithPlus: String
    private lateinit var txtTerms: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val intent: Intent = Intent(this@PhoneNumberActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        supportActionBar?.hide()

        etPhoneNumber = binding.etPhoneNumber
        ccp = binding.ccp
        ccp.registerPhoneNumberTextView(etPhoneNumber)
        btnVerify = binding.btnGetStarted
        txtTerms = binding.txtTerms

        txtTerms.movementMethod = LinkMovementMethod.getInstance()

        btnVerify.setOnClickListener{
            if (etPhoneNumber.text.toString().isEmpty() || TextUtils.isEmpty(etPhoneNumber.toString())) {
                etPhoneNumber.error = "Please Enter your Phone Number."
                Toast.makeText(this@PhoneNumberActivity, "Phone Number can not be empty!", Toast.LENGTH_LONG).show()
            } else {
                countryCodeWithPlus = ccp.selectedCountryCodeWithPlus.toString()
                val finalNumber = countryCodeWithPlus + " " + etPhoneNumber.text.toString()
                Toast.makeText(this@PhoneNumberActivity, finalNumber, Toast.LENGTH_LONG).show()
                val intent = Intent(this@PhoneNumberActivity, OTPActivity::class.java)
                intent.putExtra("phone", finalNumber)
                startActivity(intent)
            }
        }

    }
}