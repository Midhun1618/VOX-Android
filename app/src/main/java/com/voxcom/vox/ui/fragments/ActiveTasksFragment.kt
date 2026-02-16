package com.voxcom.vox.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration
import com.voxcom.vox.R
import com.voxcom.vox.data.model.Task
import com.voxcom.vox.data.repository.TaskRepository
import com.voxcom.vox.ui.dialog.CompleteTaskDialog
import com.voxcom.vox.ui.adapters.TaskAdapter

class ActiveTasksFragment : Fragment(R.layout.fragment_active_tasks) {

    private lateinit var adapter: TaskAdapter
    private var listener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val empty = view.findViewById<View>(R.id.emptyState)

        val recycler = view.findViewById<RecyclerView>(R.id.rvActiveTasks)

        adapter = TaskAdapter(mutableListOf()) { task ->
            CompleteTaskDialog(requireContext(), task.id).show()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        listener = TaskRepository.listenTasks { tasks ->
            val active = tasks.filter { !it.completed && !isExpired(it) }
            adapter.update(active)
            empty.visibility = if (active.isEmpty()) View.VISIBLE else View.GONE
        }

    }

    private fun isExpired(task: Task): Boolean {
        val now = System.currentTimeMillis()
        return task.expiresAt?.toDate()?.time ?: Long.MAX_VALUE < now
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
