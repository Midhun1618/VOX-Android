package com.voxcom.vox

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    // 🔥 RecyclerView data
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        uid = user.uid
        db = FirebaseFirestore.getInstance()

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val etTask = findViewById<EditText>(R.id.etTask)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val tvStats = findViewById<TextView>(R.id.tvStats)
        val rvTasks = findViewById<RecyclerView>(R.id.rvTasks)

        tvEmail.text = user.email ?: "No email found"

        // 🧱 RecyclerView setup
        adapter = TaskAdapter(taskList) { task ->
            showCompleteTaskDialog(task.id)
        }

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        // 🔥 Cleanup expired tasks
        cleanupExpiredTasks()

        // 🔄 Load tasks realtime
        loadTasks()

        // 📊 Load stats
        loadStats(tvStats)

        // ➕ Add task
        btnAdd.setOnClickListener {
            val title = etTask.text.toString().trim()
            if (title.isEmpty()) return@setOnClickListener

            val now = Timestamp.now()
            val expiresAt = Timestamp(
                now.seconds + TimeUnit.HOURS.toSeconds(24),
                0
            )

            val task = hashMapOf(
                "title" to title,
                "createdAt" to now,
                "expiresAt" to expiresAt,
                "completed" to false
            )

            db.collection("users")
                .document(uid)
                .collection("tasks")
                .add(task)

            db.collection("users")
                .document(uid)
                .update("totalTasks", FieldValue.increment(1))

            etTask.text.clear()
            loadStats(tvStats)
        }
    }

    // ⏰ Delete expired & not completed tasks
    private fun cleanupExpiredTasks() {
        val now = Timestamp.now()

        db.collection("users")
            .document(uid)
            .collection("tasks")
            .whereLessThan("expiresAt", now)
            .whereEqualTo("completed", false)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }
    }

    // 🔄 Load tasks in realtime
    private fun loadTasks() {
        db.collection("users")
            .document(uid)
            .collection("tasks")
            .whereEqualTo("completed", false)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                taskList.clear()
                for (doc in snapshot.documents) {
                    val task = Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        completed = doc.getBoolean("completed") ?: false,
                        expiresAt = doc.getTimestamp("expiresAt")
                    )
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
            }
    }

    // 📊 Load discipline stats
    private fun loadStats(tv: TextView) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val total = doc.getLong("totalTasks") ?: 0
                val completed = doc.getLong("completedTasks") ?: 0

                val discipline =
                    if (total == 0L) 0
                    else ((completed.toDouble() / total) * 100).toInt()

                tv.text = """
                    Total: $total
                    Completed: $completed
                    Discipline: $discipline%
                """.trimIndent()
            }
    }

    // 🎨 Custom completion dialog
    private fun showCompleteTaskDialog(taskId: String) {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_complete_task,
            null
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            markTaskCompleted(taskId)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // ✅ Mark task as completed
    private fun markTaskCompleted(taskId: String) {
        db.collection("users")
            .document(uid)
            .collection("tasks")
            .document(taskId)
            .update("completed", true)
            .addOnSuccessListener {
                db.collection("users")
                    .document(uid)
                    .update("completedTasks", FieldValue.increment(1))
            }
    }
}
