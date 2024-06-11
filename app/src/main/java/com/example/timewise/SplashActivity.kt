package com.example.timewise

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timewise.auth.LoginActivity
import com.example.timewise.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    var fromTop: Animation? = null
    var fromBottom:Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fromTop = AnimationUtils.loadAnimation(this, R.anim.fromtop)
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.frombottom)

        binding.cvLogo.animation = fromTop
        binding.tvAppName.animation = fromBottom

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                } catch (e: InterruptedException) {
                    Toast.makeText(this@SplashActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        thread.start()
    }
}