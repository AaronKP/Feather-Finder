package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class BirdGuideLocation : AppCompatActivity() {

    private lateinit var loadingScreen: View
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird_guide_location)

        val receivedCode = intent.getStringExtra("code")

        loadingScreen = findViewById(R.id.loadingScreen)
        webView = findViewById(R.id.webView)

        // Enable JavaScript (optional)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Set a WebViewClient to handle when links are clicked
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Show the loading screen when the page starts loading
                loadingScreen.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide the loading screen when the page finishes loading
                loadingScreen.visibility = View.GONE
            }
        }

        if (receivedCode == "TableMountain") {
            // Load a webpage
            webView.loadUrl("https://avibase.bsc-eoc.org/checklist.jsp?region=ZAwc0003")
        }
        else if(receivedCode == "karoo"){
            // Load a webpage
            webView.loadUrl("https://avibase.bsc-eoc.org/checklist.jsp?lang=EN&p2=1&list=clements&synlang=&region=ZAwc0004&version=text&lifelist=&highlight=0")
        }
        else if(receivedCode == "vaalbos" ){
            // Load a webpage
            webView.loadUrl("https://avibase.bsc-eoc.org/checklist.jsp?lang=EN&p2=1&list=clements&synlang=&region=ZAnc0006&version=text&lifelist=&highlight=0")
        }
        else if(receivedCode == "krugernational"){
            // Load a webpage
            webView.loadUrl("https://avibase.bsc-eoc.org/checklist.jsp?region=ZA000003")
        }
        else{
            webView.loadUrl("https://avibase.bsc-eoc.org/avibase.jsp?lang=EN")
        }


        //bottom nav
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_item -> {
                    val intent1 = Intent(this, SouthAfricanBirdGuide::class.java)
                    startActivity(intent1)
                    true
                }
                R.id.home_item -> {
                    // Handle item2 click
                    val intent2 = Intent(this, MainActivity::class.java)
                    startActivity(intent2)
                    true
                }
                R.id.map_item -> {
                    // Handle item3 click
                    val intent3 = Intent(this, MapsActivity::class.java)
                    startActivity(intent3)
                    true
                }
                else -> false
            }
        }
    }
}