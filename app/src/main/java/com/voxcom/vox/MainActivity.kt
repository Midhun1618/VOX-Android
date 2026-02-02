package com.voxcom.vox

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
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
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val avatarDrawables = listOf(
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8
    )

    private lateinit var api: WeatherApi
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    // 🔥 RecyclerView data
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(WeatherApi::class.java)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }


        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        uid = user.uid
        db = FirebaseFirestore.getInstance()
        println("🔑 API KEY = '${BuildConfig.WEATHER_API_KEY}'")

        val loadToggle = findViewById<ImageView>(R.id.loadBtn)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val myAvatar = findViewById<ImageView>(R.id.myAvatar)
        val etTask = findViewById<EditText>(R.id.etTask)
        val btnAdd = findViewById<TextView>(R.id.btnAdd)
        val tvStats = findViewById<TextView>(R.id.tvStats)
        val rvTasks = findViewById<RecyclerView>(R.id.rvTasks)
        val tvCurrentTime = findViewById<TextView>(R.id.tvCurrentTime)
        val tvCurrentMeridian = findViewById<TextView>(R.id.tvCurrentMeridian)
        val tvWeather = findViewById<TextView>(R.id.tvWeather)

        tvEmail.text = user.email ?: "No email found"

        adapter = TaskAdapter(taskList) { task ->
            showCompleteTaskDialog(task.id)
        }

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        startClock(tvCurrentTime,tvCurrentMeridian)
        ensureUserDocument()
        loadUserProfile(tvUsername, myAvatar)
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
        loadToggle.setOnClickListener {
            playClickSound()
            val handler = Handler(Looper.getMainLooper())
            var count = 0

            val runnable = object : Runnable {
                override fun run() {

                    if (count % 2 == 0) {
                        loadToggle.setImageResource(R.drawable.load1)
                    } else {
                        loadToggle.setImageResource(R.drawable.load2)
                    }

                    count++

                    if (count < 6) {
                        handler.postDelayed(this, 700)
                    }
                }
            }

            handler.post(runnable)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                getWeatherByLocation(lat, lon,tvWeather)
            }
        }
    }
    fun getWeatherByLocation(lat: Double, lon: Double,tv: TextView) {
        lifecycleScope.launch {
            val response = api.getWeatherByLatLon(
                lat,
                lon,
                BuildConfig.WEATHER_API_KEY
            )

            if (response.isSuccessful) {
                val temp = response.body()?.main?.temp
                tv.text = "$temp°C"

            }
        }
    }

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

                tvTime.text = timeFormat.format(now)
                tvAmPm.text = amPmFormat.format(now)

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }
    private fun playClickSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.onclick01_sfx)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }

    private fun loadUserProfile(
        tvUsername: TextView,
        avatarView: ImageView
    ) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val username = doc.getString("username")
                val avatarIndex = doc.getLong("avatarIndex")?.toInt()

                tvUsername.text = "Name :$username" ?: "User"

                if (avatarIndex != null && avatarIndex in avatarDrawables.indices) {
                    avatarView.setImageResource(avatarDrawables[avatarIndex])
                }
            }
    }


}
