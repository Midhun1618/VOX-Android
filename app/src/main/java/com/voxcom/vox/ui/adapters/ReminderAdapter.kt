package com.voxcom.vox.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voxcom.vox.data.model.Reminder
import com.voxcom.vox.data.model.Task
import com.voxcom.vox.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter : RecyclerView.Adapter<ReminderAdapter.VH>() {

    private val items = mutableListOf<Reminder>()

    fun submit(list: List<Reminder>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val bind: ItemReminderBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = items[position]

        holder.bind.tvTitle.text = item.title

        val sdf = SimpleDateFormat("dd MMM \n hh:mm a", Locale.getDefault())
        holder.bind.tvTime.text =
            item.time?.let { sdf.format(Date(it)) } ?: "--"
    }
}
