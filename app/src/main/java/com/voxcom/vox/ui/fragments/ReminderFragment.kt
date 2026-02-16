package com.voxcom.vox.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voxcom.vox.R
import com.voxcom.vox.data.ReminderManager
import com.voxcom.vox.ui.adapters.ReminderAdapter

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    private lateinit var rvReminders: RecyclerView
    private lateinit var adapter: ReminderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvReminders = view.findViewById(R.id.rvReminders)

        adapter = ReminderAdapter()

        rvReminders.layoutManager = LinearLayoutManager(requireContext())
        rvReminders.adapter = adapter

        // OBSERVE MEMORY (not firestore directly)
        ReminderManager.observe {
            adapter.submit(ReminderManager.all())
        }
    }
}
