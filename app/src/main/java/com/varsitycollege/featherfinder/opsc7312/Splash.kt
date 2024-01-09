package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.VideoView

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Set up the video view
        val videoView: VideoView = findViewById(R.id.videoView)
        val videoPath = "android.resource://${packageName}/${R.raw.splashscreen}"
        val videoUri = Uri.parse(videoPath)
        videoView.setVideoURI(videoUri)

        // Start playing the video
        videoView.start()

        // Create a handler to introduce a delay before playing the sound
        val handler = Handler()
        handler.postDelayed({
            // Play the sound after a 2-second delay
            val mediaPlayer = MediaPlayer.create(this, R.raw.eaglesnd)
            mediaPlayer.start()

            // Set a listener to release the MediaPlayer when playback completes
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
        }, 1000) // 1000 milliseconds = 1 second

        // Set a listener to launch your main activity when the video finishes
        videoView.setOnCompletionListener {

            startActivity(Intent(this, welcome::class.java))

            // Finish the splash activity
            finish()
        }
    }
}