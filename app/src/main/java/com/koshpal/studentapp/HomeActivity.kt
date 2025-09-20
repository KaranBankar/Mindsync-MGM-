package com.koshpal.studentapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    
    private lateinit var tvWelcomeUser: TextView
    private lateinit var tvUserMobile: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initViews()
        displayUserInfo()
    }
    
    private fun initViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        tvUserMobile = findViewById(R.id.tvUserMobile)
    }
    
    private fun displayUserInfo() {
        val userName = intent.getStringExtra("USER_NAME") ?: "Student"
        val userMobile = intent.getStringExtra("USER_MOBILE") ?: ""
        
        tvWelcomeUser.text = "Welcome, $userName!"
        tvUserMobile.text = "Mobile: +91 $userMobile"
    }
}
