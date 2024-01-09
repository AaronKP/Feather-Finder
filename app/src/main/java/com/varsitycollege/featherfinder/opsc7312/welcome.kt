package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var iv1: CardView
private lateinit var iv2: CardView
private lateinit var iv3: CardView
private lateinit var iv4: CardView
private lateinit var iv5: CardView
private lateinit var iv6: CardView

private lateinit var imagetxt: TextView

private lateinit var viewPager2: ViewPager2

private lateinit var auth: FirebaseAuth

class welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the ActionBar
        supportActionBar?.hide()
        auth= Firebase.auth

        setContentView(R.layout.activity_welcome)

        val signUp = findViewById<Button>(R.id.signUpbtn)

        signUp.setOnClickListener(){
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val signIn = findViewById<Button>(R.id.signInbtn)

        signIn.setOnClickListener(){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        viewPager2= findViewById(R.id.viewPager2)
        iv1 = findViewById(R.id.im1)
        iv2 = findViewById(R.id.im2)
        iv3 = findViewById(R.id.im3)
        iv4 = findViewById(R.id.im4)
        iv5 = findViewById(R.id.im5)
        iv6 = findViewById(R.id.im6)

        imagetxt = findViewById(R.id.imagetxt)

        val images = listOf(R.drawable.img1, R.drawable.img2, R.drawable.img3, R.drawable.img4, R.drawable.img5, R.drawable.img6)
        val adapter = ViewPagerAdapter(images)
        viewPager2.adapter = adapter

        //chnaging color of dot indicater when image is on current image
        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                changeColor()
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                changeColor()
            }

        })
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

    override fun onBackPressed() {
        // Do nothing when the back button is pressed after logging out
        // or perform any specific action you want
    }

    fun changeColor(){
        when (viewPager2.currentItem)
        {
            0->
            {
                imagetxt.setText("Customize preferences, get routes for bird-watching")
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }

            1->
            {
                imagetxt.setText("Find birding spots with directions on a map")
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }

            2->
            {
                imagetxt.setText("Easily record bird data and location")
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }

            3->
            {
                imagetxt.setText("Identify birds, learn about species")
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }

            4->
            {
                imagetxt.setText("Connect and explore South Africa's birds")
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }

            5->
            {
                imagetxt.setText("Engage with South Africa's bird-watching community")
                iv6.setCardBackgroundColor(applicationContext.resources.getColor(R.color.light_orange))
                iv2.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv3.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv4.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv5.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
                iv1.setCardBackgroundColor(applicationContext.resources.getColor(R.color.white))
            }
        }
    }
}