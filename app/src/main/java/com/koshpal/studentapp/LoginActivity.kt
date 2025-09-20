package com.koshpal.studentapp

import android.content.Context
import android.content.Intent
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    
    private lateinit var tilName: TextInputLayout
    private lateinit var tilMobile: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etMobile: TextInputEditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val TAG = "LoginActivity"
        private const val PREF_NAME = "StudentAppPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_MOBILE = "userMobile"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initSharedPreferences()
        
        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToHomeDirectly()
            return
        }
        
        initFirebase()
        initViews()
        setupClickListeners()
    }
    
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    private fun saveUserSession(name: String, mobile: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_MOBILE, mobile)
            apply()
        }
        Log.d(TAG, "User session saved: $name, $mobile")
    }
    
    private fun navigateToHomeDirectly() {
        val userName = sharedPreferences.getString(KEY_USER_NAME, "Student") ?: "Student"
        val userMobile = sharedPreferences.getString(KEY_USER_MOBILE, "") ?: ""
        
        Log.d(TAG, "User already logged in, navigating to home")
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_MOBILE", userMobile)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun initFirebase() {
        try {
            // Initialize Firebase if not already initialized
            FirebaseApp.initializeApp(this)
            
            // Get Firebase Database instance
            val firebaseDatabase = FirebaseDatabase.getInstance()
            database = firebaseDatabase.reference
            
            // Enable offline persistence (optional but recommended)
            try {
                firebaseDatabase.setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.w(TAG, "Persistence already enabled or failed to enable", e)
            }
            
            Log.d(TAG, "Firebase initialized successfully")
            Log.d(TAG, "Database URL: ${firebaseDatabase.app.options.databaseUrl}")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
            showError("Firebase initialization failed: ${e.message}")
        }
    }
    
    private fun initViews() {
        tilName = findViewById(R.id.tilName)
        tilMobile = findViewById(R.id.tilMobile)
        etName = findViewById(R.id.etName)
        etMobile = findViewById(R.id.etMobile)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            Log.d(TAG, "Submit button clicked")
            if (validateInputs()) {
                val name = etName.text.toString().trim()
                val mobile = etMobile.text.toString().trim()
                Log.d(TAG, "Registering student: Name=$name, Mobile=$mobile")
                registerStudent(name, mobile)
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        
        // Reset previous errors
        tilName.error = null
        tilMobile.error = null
        
        var isValid = true
        
        // Validate name
        if (name.isEmpty()) {
            tilName.error = "Please enter your full name"
            isValid = false
        } else if (name.length < 2) {
            tilName.error = "Name must be at least 2 characters"
            isValid = false
        } else if (!name.matches(Regex("^[a-zA-Z\\s]+$"))) {
            tilName.error = "Name should contain only letters and spaces"
            isValid = false
        }
        
        // Validate mobile number
        if (mobile.isEmpty()) {
            tilMobile.error = "Please enter your mobile number"
            isValid = false
        } else if (mobile.length != 10) {
            tilMobile.error = "Mobile number must be 10 digits"
            isValid = false
        } else if (!mobile.matches(Regex("^[6-9]\\d{9}$"))) {
            tilMobile.error = "Please enter a valid Indian mobile number"
            isValid = false
        }
        
        Log.d(TAG, "Input validation result: $isValid")
        return isValid
    }
    
    private fun registerStudent(name: String, mobile: String) {
        showLoading(true)
        Log.d(TAG, "Starting registration process...")
        
        // Check if mobile number already exists
        database.child("students").child(mobile)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Database query completed. Exists: ${snapshot.exists()}")
                    
                    if (snapshot.exists()) {
                        // Mobile number already exists, navigate to home
                        showLoading(false)
                        val existingStudent = snapshot.getValue(Student::class.java)
                        Log.d(TAG, "Existing student found: ${existingStudent?.name}")
                        
                        if (existingStudent != null) {
                            showSuccess("Welcome back, ${existingStudent.name}!")
                            saveUserSession(existingStudent.name, mobile)
                            navigateToHome(existingStudent.name, mobile)
                        } else {
                            showSuccess("Welcome back!")
                            saveUserSession(name, mobile)
                            navigateToHome(name, mobile)
                        }
                    } else {
                        // Mobile number doesn't exist, create new student
                        Log.d(TAG, "New student, saving to database...")
                        saveStudentToDatabase(name, mobile)
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database query cancelled", error.toException())
                    showLoading(false)
                    showError("Database error: ${error.message}")
                }
            })
    }
    
    private fun saveStudentToDatabase(name: String, mobile: String) {
        Log.d(TAG, "Saving student to database...")
        val student = Student.createStudent(name, mobile)
        
        Log.d(TAG, "Student object created: $student")
        
        database.child("students").child(mobile).setValue(student)
            .addOnSuccessListener {
                Log.d(TAG, "Student saved successfully to Firebase")
                showLoading(false)
                showSuccess("Registration successful!")
                saveUserSession(name, mobile)
                navigateToHome(name, mobile)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save student to Firebase", exception)
                showLoading(false)
                showError("Registration failed: ${exception.message}")
                
                // Additional debugging
                when (exception) {
                    is com.google.firebase.database.DatabaseException -> {
                        showError("Database Exception: Check your Firebase rules and internet connection")
                    }
                    is java.net.UnknownHostException -> {
                        showError("Network Error: Please check your internet connection")
                    }
                    else -> {
                        showError("Unknown error: ${exception.javaClass.simpleName}")
                    }
                }
            }
    }
    
    private fun navigateToHome(name: String, mobile: String) {
        Log.d(TAG, "Navigating to HomeActivity")
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_NAME", name)
            putExtra("USER_MOBILE", mobile)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            btnSubmit.text = ""
            btnSubmit.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnSubmit.text = "Continue"
            btnSubmit.isEnabled = true
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