package com.voxcom.vox.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.voxcom.vox.R
import com.voxcom.vox.data.model.Task
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private var tasks: List<Task>,
    private val onClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskVH>() {

    inner class TaskVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tvTaskTitle)
        private val timeLeft = view.findViewById<TextView>(R.id.tvTimeLeft)

        fun bind(task: Task) {
            title.text = task.title
            timeLeft.text = calculateTimeLeft(task.expiresAt)

            itemView.setOnClickListener {
                onClick(task)
                true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskVH(view)
    }

    override fun onBindViewHolder(holder: TaskVH, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun update(newTasks: List<Task>) {
        tasks = newTasks.sortedBy { it.expiresAt?.toDate()?.time ?: 0 }
        notifyDataSetChanged()
    }

    private fun calculateTimeLeft(expiresAt: Timestamp?): String {
        if (expiresAt == null) return "--"

        val diff = expiresAt.toDate().time - System.currentTimeMillis()
        if (diff <= 0) return "Expired"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        return "${hours}h ${minutes}m"
    }
}