package com.example.timewise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.timewise.databinding.ActivityLoginBinding
import com.example.timewise.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}