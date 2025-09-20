package com.koshpal.studentapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class BookingAdapter(
    private val bookings: List<CounselorBooking>,
    private val onItemClick: (CounselorBooking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBooking: CardView = itemView.findViewById(R.id.cardBooking)
        val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvCollege: TextView = itemView.findViewById(R.id.tvCollege)
        val tvProblemPreview: TextView = itemView.findViewById(R.id.tvProblemPreview)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        
        with(holder) {
            tvBookingId.text = "ID: ${booking.bookingId.takeLast(8)}" // Show last 8 characters
            tvDate.text = booking.preferredDate
            tvTime.text = booking.preferredTime
            tvCollege.text = booking.collegeName
            tvBookingDate.text = "Booked on: ${booking.bookingDate.split(" ")[0]}" // Show only date part
            
            // Show problem preview (first 50 characters)
            val problemPreview = if (booking.problemDescription.length > 50) {
                "${booking.problemDescription.take(50)}..."
            } else {
                booking.problemDescription
            }
            tvProblemPreview.text = problemPreview
            
            // Set status chip
            setupStatusChip(chipStatus, booking.status)
            
            // Set click listener
            cardBooking.setOnClickListener {
                onItemClick(booking)
            }
        }
    }

    override fun getItemCount(): Int = bookings.size

    private fun setupStatusChip(chip: Chip, status: String) {
        chip.text = status.uppercase()
        
        val context = chip.context
        when (status.lowercase()) {
            CounselorBooking.STATUS_PENDING -> {
                chip.setChipBackgroundColorResource(R.color.status_pending_bg)
                chip.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
            }
            CounselorBooking.STATUS_CONFIRMED -> {
                chip.setChipBackgroundColorResource(R.color.status_confirmed_bg)
                chip.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed_text))
            }
            CounselorBooking.STATUS_COMPLETED -> {
                chip.setChipBackgroundColorResource(R.color.status_completed_bg)
                chip.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text))
            }
            CounselorBooking.STATUS_CANCELLED -> {
                chip.setChipBackgroundColorResource(R.color.status_cancelled_bg)
                chip.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled_text))
            }
            else -> {
                chip.setChipBackgroundColorResource(R.color.status_pending_bg)
                chip.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
            }
        }
    }
}
