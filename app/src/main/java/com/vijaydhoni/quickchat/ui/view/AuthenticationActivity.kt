package com.vijaydhoni.quickchat.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.ActivityMainBinding

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}