package com.voxcom.vox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private val tasks: List<Task>,
    private val onLongPress: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTimeLeft: TextView = itemView.findViewById(R.id.tvTimeLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTitle.text = task.title
        holder.tvTimeLeft.text = calculateTimeLeft(task.expiresAt)

        holder.itemView.setOnLongClickListener {
            onLongPress(task)
            true
        }
    }

    override fun getItemCount(): Int = tasks.size

    // ⏳ Time left calculation
    private fun calculateTimeLeft(expiresAt: Timestamp?): String {
        if (expiresAt == null) return "--"

        val now = System.currentTimeMillis()
        val expiryMillis = expiresAt.toDate().time
        val diff = expiryMillis - now

        if (diff <= 0) return "Expired"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

        return "${hours}h ${minutes}m"
    }

}
