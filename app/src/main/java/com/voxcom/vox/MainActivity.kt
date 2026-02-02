package com.voxcom.vox

import android.app.AlertDialog
import android.media.MediaPlayer
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
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
        val btnAdd = findViewById<TextView>(R.id.btnAdd)
        val tvStats = findViewById<TextView>(R.id.tvStats)
        val rvTasks = findViewById<RecyclerView>(R.id.rvTasks)
        val tvCurrentTime = findViewById<TextView>(R.id.tvCurrentTime)
        val tvCurrentMeridian = findViewById<TextView>(R.id.tvCurrentMeridian)


        tvEmail.text = user.email ?: "No email found"

        // 🧱 RecyclerView setup
        adapter = TaskAdapter(taskList) { task ->
            showCompleteTaskDialog(task.id)
        }

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter


        startClock(tvCurrentTime,tvCurrentMeridian)
        ensureUserDocument()
        // 🔥 Cleanup expired tasks
        cleanupExpiredTasks()

        // 🔄 Load tasks realtime
        loadTasks()

        // 📊 Load stats
        loadStats(tvStats)

        // ➕ Add task
        btnAdd.setOnClickListener {
            playClickSound()
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
                .set(
                    mapOf("totalTasks" to FieldValue.increment(1)),
                    SetOptions.merge()
                )

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
            .whereEqualTo("completed", false) // 👈 KEY LINE
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                taskList.clear()
                for (doc in snapshot.documents) {
                    val task = Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        completed = false,
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

        val btnYes = dialogView.findViewById<TextView>(R.id.btnYes)
        val btnNo = dialogView.findViewById<TextView>(R.id.btnNo)

        btnYes.setOnClickListener {
            markTaskCompleted(taskId)
            playClickSound()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            playClickSound()
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
    private fun ensureUserDocument() {
        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val data = hashMapOf(
                    "email" to FirebaseAuth.getInstance().currentUser?.email,
                    "totalTasks" to 0,
                    "completedTasks" to 0,
                    "createdAt" to Timestamp.now()
                )
                userRef.set(data)
            }
        }
    }
    private fun startClock(tvTime: TextView, tvAmPm: TextView) {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()

                val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

                tvTime.text = timeFormat.format(now)   // 11:11
                tvAmPm.text = amPmFormat.format(now)   // AM / PM

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }
    private fun playClickSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.onclick01_sfx)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release() // prevent memory leak
        }
    }

}
