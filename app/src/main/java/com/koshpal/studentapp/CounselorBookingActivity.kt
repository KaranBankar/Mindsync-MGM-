package com.koshpal.studentapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CounselorBookingActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilStudentName: TextInputLayout
    private lateinit var tilAddress: TextInputLayout
    private lateinit var tilCollege: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var tilTime: TextInputLayout
    private lateinit var tilProblem: TextInputLayout
    
    private lateinit var etStudentName: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etCollege: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var etProblem: TextInputEditText
    
    private lateinit var btnSubmitBooking: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    
    private var userName: String = ""
    private var userMobile: String = ""
    
    companion object {
        private const val TAG = "CounselorBooking"
        private const val PREF_NAME = "StudentAppPrefs"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_MOBILE = "userMobile"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_counselor_booking)
        
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
        setupClickListeners()
        prefillUserData()
    }
    
    private fun initFirebase() {
        database = FirebaseDatabase.getInstance().reference
    }
    
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tilStudentName = findViewById(R.id.tilStudentName)
        tilAddress = findViewById(R.id.tilAddress)
        tilCollege = findViewById(R.id.tilCollege)
        tilDate = findViewById(R.id.tilDate)
        tilTime = findViewById(R.id.tilTime)
        tilProblem = findViewById(R.id.tilProblem)
        
        etStudentName = findViewById(R.id.etStudentName)
        etAddress = findViewById(R.id.etAddress)
        etCollege = findViewById(R.id.etCollege)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etProblem = findViewById(R.id.etProblem)
        
        btnSubmitBooking = findViewById(R.id.btnSubmitBooking)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun getUserData() {
        // Get user data from Intent or SharedPreferences
        userName = intent.getStringExtra("USER_NAME") 
            ?: sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        userMobile = intent.getStringExtra("USER_MOBILE") 
            ?: sharedPreferences.getString(KEY_USER_MOBILE, "") ?: ""
        
        Log.d(TAG, "User data: Name=$userName, Mobile=$userMobile")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Book Counselor Session"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun prefillUserData() {
        etStudentName.setText(userName)
    }
    
    private fun setupClickListeners() {
        etDate.setOnClickListener {
            showDatePicker()
        }
        
        etTime.setOnClickListener {
            showTimePicker()
        }
        
        btnSubmitBooking.setOnClickListener {
            if (validateInputs()) {
                submitBooking()
            }
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                
                // Check if selected date is not in the past
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                
                if (selectedDate.before(today)) {
                    Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(selectedDate.time))
            },
            year, month, day
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)
                
                etTime.setText(timeFormat.format(selectedTime.time))
            },
            hour, minute, true
        )
        
        timePickerDialog.show()
    }
    
    private fun validateInputs(): Boolean {
        val name = etStudentName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val college = etCollege.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val problem = etProblem.text.toString().trim()
        
        // Reset errors
        tilStudentName.error = null
        tilAddress.error = null
        tilCollege.error = null
        tilDate.error = null
        tilTime.error = null
        tilProblem.error = null
        
        var isValid = true
        
        if (name.isEmpty()) {
            tilStudentName.error = "Please enter your name"
            isValid = false
        }
        
        if (address.isEmpty()) {
            tilAddress.error = "Please enter your address"
            isValid = false
        }
        
        if (college.isEmpty()) {
            tilCollege.error = "Please enter your college name"
            isValid = false
        }
        
        if (date.isEmpty()) {
            tilDate.error = "Please select a date"
            isValid = false
        }
        
        if (time.isEmpty()) {
            tilTime.error = "Please select a time"
            isValid = false
        }
        
        if (problem.isEmpty()) {
            tilProblem.error = "Please describe your problem"
            isValid = false
        } else if (problem.length < 10) {
            tilProblem.error = "Please provide more details (minimum 10 characters)"
            isValid = false
        }
        
        return isValid
    }
    
    private fun submitBooking() {
        showLoading(true)
        
        val booking = CounselorBooking.createBooking(
            studentName = etStudentName.text.toString().trim(),
            studentMobile = userMobile,
            studentAddress = etAddress.text.toString().trim(),
            collegeName = etCollege.text.toString().trim(),
            preferredDate = etDate.text.toString().trim(),
            preferredTime = etTime.text.toString().trim(),
            problemDescription = etProblem.text.toString().trim()
        )
        
        Log.d(TAG, "Submitting booking: $booking")
        
        // Store booking under user's mobile number and also in a general bookings collection
        val bookingRef = database.child("bookings").child(userMobile).child(booking.bookingId)
        val allBookingsRef = database.child("all_bookings").child(booking.bookingId)
        
        // Save to both locations
        bookingRef.setValue(booking)
            .addOnSuccessListener {
                // Also save to all bookings for admin access
                allBookingsRef.setValue(booking)
                    .addOnSuccessListener {
                        showLoading(false)
                        showSuccess("Booking submitted successfully!")
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to save to all bookings", exception)
                        showLoading(false)
                        showError("Booking submitted but failed to update admin records")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to submit booking", exception)
                showLoading(false)
                showError("Failed to submit booking: ${exception.message}")
            }
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            btnSubmitBooking.text = ""
            btnSubmitBooking.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnSubmitBooking.text = "Submit Booking"
            btnSubmitBooking.isEnabled = true
        }
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Success: $message")
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Error: $message")
    }
}
