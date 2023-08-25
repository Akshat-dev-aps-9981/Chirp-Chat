package com.aksapps.chirpchat.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aksapps.chirpchat.R
import com.aksapps.chirpchat.databinding.ActivityOtpBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mukeshsolanki.OtpView
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private lateinit var agreementCheck: MaterialCheckBox
    private lateinit var btnGetStarted: MaterialButton
    private lateinit var otpView: OtpView
    private lateinit var txtUserPhoneNumber: TextView
    private var phoneT: String? = null
    private var phoneN: String? = null

    private lateinit var progressDialog: AlertDialog.Builder
    private lateinit var alertProgress: AlertDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        progressDialog = AlertDialog.Builder(this@OTPActivity as Context)
            .setView(R.layout.progress_dialog)
            .setCancelable(false)

        alertProgress = progressDialog.create()

        alertProgress.show()

        if (intent != null) {
            phoneT = intent.getStringExtra("phone") ?: "Not Available."
            phoneN = phoneT?.replace(Regex("[^\\d]"), "")
        }

        agreementCheck = binding.agreementCheck
        btnGetStarted = binding.btnGetStarted
        otpView = binding.otpView
        txtUserPhoneNumber = binding.txtUserPhoneNumber

        firebaseAuth = FirebaseAuth.getInstance()

        val phoneAuthOptions = phoneN?.let {
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+$it")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this@OTPActivity as Activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        Log.w("OnVerification", "Completed")
                    }

                    override fun onVerificationFailed(firebaseException: FirebaseException) {
                        Log.w("OnVerification", "Failed")
                    }

                    override fun onCodeSent(
                        verifyId: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = verifyId
                        alertProgress.dismiss()
                        Toast.makeText(
                            this@OTPActivity,
                            "Code sent successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        binding.otpView.requestFocus()
                        super.onCodeSent(verifyId, forceResendingToken)
                    }

                }).build()
        }

        if (phoneAuthOptions != null) {
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
        } else {
            Toast.makeText(this@OTPActivity, "Phone auth options is null.", Toast.LENGTH_SHORT)
                .show()
        }

        val prev = txtUserPhoneNumber.text.toString()
        txtUserPhoneNumber.text = prev + phoneT + "."

        agreementCheck.movementMethod = LinkMovementMethod.getInstance()

        /*otpView.setOtpCompletionListener {

        }*/

        btnGetStarted.setOnClickListener {
            if (otpView.text.isNullOrEmpty()) {
                otpView.error = "Please enter OTP received on your phone number."
            } else if (!agreementCheck.isChecked) {
                agreementCheck.error = "You need to give your consent to create your account."
            } else {
//                Snackbar.make(it, "You are successfully logged in.", Snackbar.LENGTH_LONG).show()
//                Toast.makeText(this@OTPActivity, "You successfully agreed to our terms & conditions.", Toast.LENGTH_SHORT).show()
                val credential: PhoneAuthCredential? =
                    verificationId?.let { it1 ->
                        PhoneAuthProvider.getCredential(
                            it1,
                            otpView.text.toString()
                        )
                    }

                if (credential != null) {
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@OTPActivity,
                                "Logged In: ${it.result}.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val  intent = Intent(this@OTPActivity, SetupProfileActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(
                                this@OTPActivity,
                                "Failed. Reason: ${it.exception.toString()}.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this@OTPActivity, "Credential is null.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }
}