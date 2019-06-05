package com.example.cis400.eventfinder

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_main.*
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.provider.MediaStore
import com.example.cis400.eventfinder.R.id.videoView
import android.content.SharedPreferences
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var sharedpreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedpreferences = applicationContext.getSharedPreferences("Preferences", 0)
        val login = sharedpreferences.getString("LOGIN", null)
        if (login != null) {
            val email = sharedpreferences.getString("EMAIL", null)
            val password = sharedpreferences.getString("PASSWORD", null)
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                    }
        }

        val videoview = findViewById<VideoView>(R.id.videoView)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.videoplayback)

        videoview.setVideoURI(uri)
        videoview.start()


        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
        }



        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
    }

    override fun onBackPressed() {
        //do nothing
    }

    override fun onStop(){
        super.onStop()
        finish()
    }

}