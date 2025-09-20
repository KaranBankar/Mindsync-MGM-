package com.koshpal.studentapp

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class CounselorBooking(
    val bookingId: String = "",
    val studentName: String = "",
    val studentMobile: String = "",
    val studentAddress: String = "",
    val collegeName: String = "",
    val preferredDate: String = "",
    val preferredTime: String = "",
    val problemDescription: String = "",
    val bookingDate: String = "",
    val status: String = "pending", // pending, confirmed, completed, cancelled
    val counselorId: String = "",
    val counselorName: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "", "pending", "", "")
    
    companion object {
        fun createBooking(
            studentName: String,
            studentMobile: String,
            studentAddress: String,
            collegeName: String,
            preferredDate: String,
            preferredTime: String,
            problemDescription: String
        ): CounselorBooking {
            val bookingId = generateBookingId()
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            return CounselorBooking(
                bookingId = bookingId,
                studentName = studentName.trim(),
                studentMobile = studentMobile.trim(),
                studentAddress = studentAddress.trim(),
                collegeName = collegeName.trim(),
                preferredDate = preferredDate.trim(),
                preferredTime = preferredTime.trim(),
                problemDescription = problemDescription.trim(),
                bookingDate = currentDate,
                status = "pending",
                counselorId = "",
                counselorName = ""
            )
        }
        
        private fun generateBookingId(): String {
            val timestamp = System.currentTimeMillis()
            val random = (1000..9999).random()
            return "BOOK_${timestamp}_$random"
        }
        
        // Status constants
        const val STATUS_PENDING = "pending"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
    }
}
