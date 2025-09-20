package com.koshpal.studentapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initFirebase()
        initViews()
        setupClickListeners()
    }
    
    private fun initFirebase() {
        database = FirebaseDatabase.getInstance().reference
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
            if (validateInputs()) {
                val name = etName.text.toString().trim()
                val mobile = etMobile.text.toString().trim()
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
        
        return isValid
    }
    
    private fun registerStudent(name: String, mobile: String) {
        showLoading(true)
        
        // Check if mobile number already exists
        database.child("students").child(mobile)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Mobile number already exists, navigate to home
                        showLoading(false)
                        val existingStudent = snapshot.getValue(Student::class.java)
                        if (existingStudent != null) {
                            navigateToHome(existingStudent.name, mobile)
                        } else {
                            navigateToHome(name, mobile)
                        }
                    } else {
                        // Mobile number doesn't exist, create new student
                        saveStudentToDatabase(name, mobile)
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    showError("Database error: ${error.message}")
                }
            })
    }
    
    private fun saveStudentToDatabase(name: String, mobile: String) {
        val student = Student.createStudent(name, mobile)
        
        database.child("students").child(mobile).setValue(student)
            .addOnSuccessListener {
                showLoading(false)
                showSuccess("Registration successful!")
                navigateToHome(name, mobile)
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                showError("Registration failed: ${exception.message}")
            }
    }
    
    private fun navigateToHome(name: String, mobile: String) {
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
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}