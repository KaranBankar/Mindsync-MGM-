package com.koshpal.studentapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {
    
    private lateinit var tvWelcomeUser: TextView
    private lateinit var tvUserMobile: TextView
    private lateinit var cardBookCounselor: MaterialCardView
    private lateinit var btnBookCounselor: MaterialButton
    private lateinit var btnMyBookings: MaterialButton
    private lateinit var btnLogout: MaterialButton
    
    private lateinit var sharedPreferences: SharedPreferences
    private var userName: String = ""
    private var userMobile: String = ""
    
    companion object {
        private const val TAG = "HomeActivity"
        private const val PREF_NAME = "StudentAppPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_MOBILE = "userMobile"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initSharedPreferences()
        initViews()
        getUserData()
        displayUserInfo()
        setupClickListeners()
    }
    
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private fun initViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        tvUserMobile = findViewById(R.id.tvUserMobile)
        cardBookCounselor = findViewById(R.id.cardBookCounselor)
        btnBookCounselor = findViewById(R.id.btnBookCounselor)
        btnMyBookings = findViewById(R.id.btnMyBookings)
        btnLogout = findViewById(R.id.btnLogout)
    }
    
    private fun getUserData() {
        // First try to get data from Intent (for fresh login)
        userName = intent.getStringExtra("USER_NAME") ?: ""
        userMobile = intent.getStringExtra("USER_MOBILE") ?: ""
        
        // If not available in Intent, get from SharedPreferences (for returning user)
        if (userName.isEmpty() || userMobile.isEmpty()) {
            userName = sharedPreferences.getString(KEY_USER_NAME, "Student") ?: "Student"
            userMobile = sharedPreferences.getString(KEY_USER_MOBILE, "") ?: ""
        }
        
        Log.d(TAG, "User data: Name=$userName, Mobile=$userMobile")
    }
    
    private fun displayUserInfo() {
        tvWelcomeUser.text = "Welcome, $userName!"
        tvUserMobile.text = "Mobile: +91 $userMobile"
    }
    
    private fun setupClickListeners() {
        btnBookCounselor.setOnClickListener {
            Log.d(TAG, "Book Counselor clicked")
            navigateToBookingForm()
        }
        
        btnMyBookings.setOnClickListener {
            Log.d(TAG, "My Bookings clicked")
            // TODO: Navigate to bookings list activity
            Toast.makeText(this, "My Bookings feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        btnLogout.setOnClickListener {
            Log.d(TAG, "Logout clicked")
            logout()
        }
    }
    
    private fun navigateToBookingForm() {
        val intent = Intent(this, CounselorBookingActivity::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_MOBILE", userMobile)
        }
        startActivity(intent)
    }
    
    private fun logout() {
        // Clear SharedPreferences
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_NAME)
            remove(KEY_USER_MOBILE)
            apply()
        }
        
        Log.d(TAG, "User logged out")
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
