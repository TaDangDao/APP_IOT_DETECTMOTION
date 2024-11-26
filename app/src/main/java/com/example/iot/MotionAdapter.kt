package com.example.iot

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MotionAdapter(private val motionList: List<Motion>, var isAdmin: Boolean, // Biến kiểm tra quyền Admin
                    private val onDeleteClick: (String) -> Unit ):
    RecyclerView.Adapter<MotionAdapter.MotionViewHolder>() {

    class MotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val motionIcon: ImageView = itemView.findViewById(R.id.motionIcon)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val motionTextView: TextView = itemView.findViewById(R.id.motionTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val btn: Button = itemView.findViewById(R.id.vl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.motion_item, parent, false)
        return MotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MotionViewHolder, position: Int) {
        val motion = motionList[position]

        // Cập nhật biểu tượng trạng thái
        val context = holder.itemView.context
        holder.motionIcon.setImageResource(
            if (motion.motion) R.drawable.ic_motion_detected else R.drawable.ic_no_motion
        )

        // Hiển thị timestamp
        holder.timestampTextView.text = formatDay(motion.timestamp)

        // Hiển thị trạng thái chuyển động
        holder.motionTextView.text = if (motion.motion) "Motion Detected" else "No Motion"
        // Hiển thị giờ phút từ timestamp
        holder.timeTextView.text = formatTime(motion.timestamp)
        holder.btn.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.btn.setOnClickListener {
            onDeleteClick(motion.id)
        }

    }

    override fun getItemCount(): Int = motionList.size

    // Hàm định dạng giờ phút
    fun formatTime(input: String): String {
        if (input.length < 16) {
            Log.e("MotionAdapter", "Input string is too short: $input")
            return "Invalid Time"
        }
        return try {
            input.substring(11, 16) // Ensure this range is valid
        } catch (e: StringIndexOutOfBoundsException) {
            Log.e("MotionAdapter", "Error formatting time: ${e.message}")
            "Invalid Time"
        }
    }
    fun formatDay(input: String): String {
        if (input.length < 16) {
            Log.e("MotionAdapter", "Input string is too short: $input")
            return "Invalid Time"
        }
        return try {
            input.substring(0, 10) // Ensure this range is valid
        } catch (e: StringIndexOutOfBoundsException) {
            Log.e("MotionAdapter", "Error formatting time: ${e.message}")
            "Invalid Time"
        }
    }


}
