package com.varsitycollege.featherfinder.opsc7312

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the ActionBar
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        auth= Firebase.auth

        val login = findViewById<Button>(R.id.loginbtn)

        login.setOnClickListener(){

            val emailEditText = findViewById<TextInputEditText>(R.id.emailtxt)
            val emailText = emailEditText.text.toString()

            val passswordEditText = findViewById<TextInputEditText>(R.id.passwordtxt)
            val passswordText = passswordEditText.text.toString()


            auth.signInWithEmailAndPassword(emailText, passswordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val verification =  auth.currentUser?.isEmailVerified
                        if(verification == true){
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI()
                        }
                        else{
                            Toast.makeText(this, "Please verify your Email.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Invalid email address or password.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }


        //forgotten password
        val textView = findViewById<TextView>(R.id.forgotpasswordtxt)

        textView.setOnClickListener {
            val dialogFragment = MyDialogFragment()
            dialogFragment.show(supportFragmentManager, "MyDialog")
        }
    }

    private fun updateUI(){
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


//to do
//fix error messages