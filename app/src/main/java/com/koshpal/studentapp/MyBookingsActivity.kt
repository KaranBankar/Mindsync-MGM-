package com.koshpal.studentapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyBookingsActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    
    private var userMobile: String = ""
    private val bookingsList = mutableListOf<CounselorBooking>()
    
    companion object {
        private const val TAG = "MyBookingsActivity"
        private const val PREF_NAME = "StudentAppPrefs"
        private const val KEY_USER_MOBILE = "userMobile"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_bookings)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initFirebase()
        initSharedPreferences()
        initViews()
        getUserData()
        setupToolbar()
        setupRecyclerView()
        loadBookings()
    }
    
    private fun initFirebase() {
        database = FirebaseDatabase.getInstance().reference
    }
    
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewBookings)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyState = findViewById(R.id.tvEmptyState)
    }
    
    private fun getUserData() {
        userMobile = intent.getStringExtra("USER_MOBILE") 
            ?: sharedPreferences.getString(KEY_USER_MOBILE, "") ?: ""
        
        Log.d(TAG, "User mobile: $userMobile")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "My Bookings"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(bookingsList) { booking ->
            // Handle booking item click (e.g., show details)
            showBookingDetails(booking)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyBookingsActivity)
            adapter = bookingAdapter
        }
    }
    
    private fun loadBookings() {
        if (userMobile.isEmpty()) {
            showError("User mobile not found")
            return
        }
        
        showLoading(true)
        Log.d(TAG, "Loading bookings for user: $userMobile")
        
        database.child("bookings").child(userMobile)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Bookings data received: ${snapshot.childrenCount} bookings")
                    
                    bookingsList.clear()
                    
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(CounselorBooking::class.java)
                        booking?.let {
                            bookingsList.add(it)
                            Log.d(TAG, "Added booking: ${it.bookingId}")
                        }
                    }
                    
                    // Sort bookings by booking date (newest first)
                    bookingsList.sortByDescending { it.bookingDate }
                    
                    showLoading(false)
                    updateUI()
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to load bookings", error.toException())
                    showLoading(false)
                    showError("Failed to load bookings: ${error.message}")
                }
            })
    }
    
    private fun updateUI() {
        if (bookingsList.isEmpty()) {
            recyclerView.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            bookingAdapter.notifyDataSetChanged()
        }
    }
    
    private fun showBookingDetails(booking: CounselorBooking) {
        val details = """
            Booking ID: ${booking.bookingId}
            Date: ${booking.preferredDate}
            Time: ${booking.preferredTime}
            College: ${booking.collegeName}
            Status: ${booking.status.uppercase()}
            
            Problem Description:
            ${booking.problemDescription}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Booking Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Error: $message")
    }
}
