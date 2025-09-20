package com.koshpal.studentapp

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class Student(
    val name: String = "",
    val mobileNumber: String = "",
    val registrationDate: String = "",
    val isActive: Boolean = true
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", true)
    
    companion object {
        fun createStudent(name: String, mobileNumber: String): Student {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            return Student(
                name = name.trim(),
                mobileNumber = mobileNumber.trim(),
                registrationDate = currentDate,
                isActive = true
            )
        }
    }
}
