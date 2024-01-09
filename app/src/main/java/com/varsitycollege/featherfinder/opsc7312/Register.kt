package com.varsitycollege.featherfinder.opsc7312

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Hide the ActionBar
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        val btnRegister = findViewById<Button>(R.id.registerbtn)
        val emailEditText = findViewById<TextInputEditText>(R.id.emailtxt)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordtxt)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.confirmpasswordtxt)


        var isEmail = false
        var isPassword = false
        var isConfirmPassword = false

        val linkTextView = findViewById<TextView>(R.id.tcstext)

        linkTextView.setOnClickListener {
            // Create and show the dialog box
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage("Terms and Conditions for Feather Finder:\n" +
                        "\n" +
                        "By using the Feather Finder application, you agree to the following terms and conditions:\n" +
                        "\n" +
                        "1. Account Registration:\n" +
                        "   - You must create an account to access and use the App.\n" +
                        "   - You are responsible for maintaining the confidentiality of your account login credentials.\n" +
                        "   - You are solely responsible for all activities that occur under your account.\n" +
                        "\n" +
                        "2. User Responsibilities:\n" +
                        "   - You must provide accurate and complete information when using the App.\n" +
                        "   - You are responsible for the content and accuracy of the data you enter into the App.\n" +
                        "   - You must not misuse, interfere with, or disrupt the App or its associated systems.\n" +
                        "\n" +
                        "3. Data Privacy and Security:\n" +
                        "   - The App stores your data in an online database for retrieval and analysis.\n" +
                        "   - While we take reasonable measures to protect your data, we cannot guarantee absolute security.\n" +
                        "   - We may collect and use your data in accordance with our Privacy Policy.\n" +
                        "\n" +
                        "4. Availability and Updates:\n" +
                        "   - We strive to ensure that the App is available and functional, but we do not guarantee uninterrupted access.\n" +
                        "   - We may release updates to enhance the App's features and address any issues or bugs.\n" +
                        "\n" +
                        "5. Third-Party Content and Links:\n" +
                        "   - The App may contain links to third-party websites or resources.\n" +
                        "   - We are not responsible for the content, accuracy, or availability of these external resources.\n" +
                        "\n" +
                        "6. Limitation of Liability:\n" +
                        "   - To the maximum extent permitted by law, we shall not be liable for any damages arising from the use of the App.\n" +
                        "\n" +
                        "By using the Feather Finder App, you acknowledge that you have read, understood, and agreed to these terms and conditions. If you do not agree to these terms, you should refrain from using the App.\n")
                .setPositiveButton("OK") { dialog, _ ->
                    // Handle positive button click
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle negative button click
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
        }




        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.error =("Please enter a valid email address")
                    isEmail = false
                }  else {
                    emailEditText.error = null // Clear the error
                    isEmail = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()

                val passwordRegex = "(?=.*[A-Z])(?=.*[0-9]).{6,}".toRegex()

                if (!passwordRegex.matches(password)) {
                    passwordEditText.error = "Password must be at least 6 characters long, contain a capital letter, and a numerical value"
                    isPassword = false
                } else {
                    passwordEditText.error = null // Clear the error
                    isPassword = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        confirmPasswordEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = passwordEditText.text.toString() // Retrieve the password from passwordEditText

                if (confirmPassword != password) { // Compare the passwords
                    confirmPasswordEditText.error = "Passwords do not match" // Set error on confirmPassText
                    isConfirmPassword = false
                } else {
                    confirmPasswordEditText.error = null // Clear the error
                    isConfirmPassword = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        btnRegister.setOnClickListener {
            val nameEditText = findViewById<TextInputEditText>(R.id.nametxt)
            val nameText = nameEditText.text.toString()

            val emailEditText = findViewById<TextInputEditText>(R.id.emailtxt)
            val emailText = emailEditText.text.toString()

            val passswordEditText = findViewById<TextInputEditText>(R.id.passwordtxt)
            val passswordText = passswordEditText.text.toString()

            val passswordConfirmEditText = findViewById<TextInputEditText>(R.id.confirmpasswordtxt)
            val passswordConfirmText = passswordConfirmEditText.text.toString()

            val termsCheckBox = findViewById<CheckBox>(R.id.checkBox)

            if(!termsCheckBox.isChecked){
                Toast.makeText(
                    baseContext,
                    "Please agree with the terms and conditions before continuing",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Check if all fields are not empty and formatted correctly
            if (nameText.isNotEmpty() && emailText.isNotEmpty() && passswordText.isNotEmpty() && passswordConfirmText.isNotEmpty() && termsCheckBox.isChecked && isEmail && isConfirmPassword && isPassword) {
                // Check if the email is already registered
                auth.fetchSignInMethodsForEmail(emailText)
                    .addOnCompleteListener { fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val signInMethods = fetchTask.result?.signInMethods
                            if (signInMethods != null && signInMethods.isNotEmpty()) {
                                // Email is already registered
                                Toast.makeText(
                                    baseContext,
                                    "Email already registered. Go to Login Page.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Email is not registered, proceed with registration
                                auth.createUserWithEmailAndPassword(emailText, passswordText)
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            // Sign in success, update user's profile with the name
                                            val user = auth.currentUser
                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(nameText) // Set the display name
                                                .build()

                                            user?.updateProfile(profileUpdates)
                                                ?.addOnCompleteListener { profileUpdateTask ->
                                                    if (profileUpdateTask.isSuccessful) {
                                                        Log.d(ContentValues.TAG, "User profile updated.")
                                                        auth.currentUser?.sendEmailVerification()
                                                            ?.addOnSuccessListener {
                                                                Toast.makeText(this, "Email sent to $emailText. Please verify your email", Toast.LENGTH_SHORT).show()
                                                            }
                                                            ?.addOnFailureListener{
                                                                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                                                            }
                                                        //updateUI()
                                                    } else {
                                                        Log.w(ContentValues.TAG, "Failed to update user profile.")
                                                        // Handle the error here
                                                    }
                                                }
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                                            // Authentication failed because of some other reason
                                            Toast.makeText(
                                                baseContext,
                                                "Email already registered. Login instead.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        } else {
                            // Error occurred while checking if email is registered
                            Log.w(ContentValues.TAG, "fetchSignInMethodsForEmail:failure", fetchTask.exception)
                            Toast.makeText(
                                baseContext,
                                "Error checking email availability.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            } else {
                // If fields are incorrectly formatted or empty, show an error message
                Toast.makeText(
                    baseContext,
                    "Please fill in all fields correctly.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    //to-do

    //see why authentication failed (email taken)

    private fun updateUI() {
        val Intent = Intent(this, MainActivity::class.java)
        startActivity(Intent)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI()
        }
    }
}