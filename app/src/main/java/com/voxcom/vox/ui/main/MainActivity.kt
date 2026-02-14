package com.voxcom.vox.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.voxcom.vox.R
import com.voxcom.vox.data.TaskManager
import com.voxcom.vox.data.repository.TaskRepository
import com.voxcom.vox.data.repository.UserRepository
import com.voxcom.vox.data.repository.WeatherRepository
import com.voxcom.vox.service.VoxService
import com.voxcom.vox.system.*
import com.voxcom.vox.ui.dialog.CompleteTaskDialog
import com.voxcom.vox.voice.VoxAssistantManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var voxManager: VoxAssistantManager
    private val clockManager = ClockManager()
    private lateinit var etTask: EditText
    private lateinit var btnAdd: TextView
    private lateinit var voxEmote: ImageView
    private lateinit var tvStats: TextView
    private lateinit var tvWeather: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvAmPm: TextView
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvDescipline: TextView
    private lateinit var avatar: ImageView
    private var taskListener: ListenerRegistration? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) initializeApp()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VoxNotification.createChannel(this)

        bindViews()
        setupRecycler()
        clockManager.start(tvTime, tvAmPm)

        requestStartupPermissions()
    }

    private fun requestStartupPermissions() {

        val permissions = mutableListOf<String>()

        if (!PermissionManager.hasMic(this))
            permissions.add(Manifest.permission.RECORD_AUDIO)

        if (!PermissionManager.hasLocation(this))
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (!PermissionManager.hasNotification(this))
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)

        if (permissions.isEmpty()) {
            initializeApp()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun initializeApp() {
        voxManager = VoxAssistantManager(this)

        bindUserStaticInfo()
        observeAppState()
        loadWeather()
        setupListeners()
    }

    private fun bindViews() {
        etTask = findViewById(R.id.etTask)
        btnAdd = findViewById(R.id.btnAdd)
        voxEmote = findViewById(R.id.voxEmote)
        tvStats = findViewById(R.id.tvStats)
        tvDescipline = findViewById(R.id.tvDisciplie)
        tvWeather = findViewById(R.id.tvWeather)
        tvTime = findViewById(R.id.tvCurrentTime)
        tvAmPm = findViewById(R.id.tvCurrentMeridian)
        rvTasks = findViewById(R.id.rvTasks)

        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        tvCode = findViewById(R.id.codeTv)
        avatar = findViewById(R.id.myAvatar)
    }

    private fun setupRecycler() {

        adapter = TaskAdapter(
            tasks = emptyList(),
            onClick = { task ->
                CompleteTaskDialog(this, task.id).show()
            }
        )

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        taskListener = TaskRepository.listenTasks { tasks ->
            TaskManager.update(tasks)
        }
    }

    private fun observeAppState() {

        TaskManager.observe { _ ->

            val activeTasks = TaskManager.active()
            adapter.update(activeTasks)

            val (total, done, missed) = TaskManager.stats()

            tvStats.text = """
            ADDED: $total
            DONE: $done
            MISSED: $missed
        """.trimIndent()

            val discipline =
                if (total == 0) 0 else ((done.toFloat() / total) * 100).toInt()

            tvDescipline.text = "$discipline%"
        }
    }


    private fun setupListeners() {

        btnAdd.setOnClickListener {
            SoundPlayer.wake(this, R.raw.onclick01_sfx)

            val title = etTask.text.toString().trim()
            if (title.isEmpty()) return@setOnClickListener

            TaskRepository.addTask(title)
            etTask.text.clear()
        }

        voxEmote.setOnClickListener {
            playVoxAnimation()
            SoundPlayer.wake(this, R.raw.waketone)
            voxManager.startListening()
        }

        voxEmote.setOnLongClickListener {
            showVoxPref()
            true
        }
    }


    private fun loadWeather() {
        LocationProvider.getLastLocation(this) { location ->
            location ?: return@getLastLocation

            lifecycleScope.launch {
                val temp = WeatherRepository.getWeather(location.latitude, location.longitude)
                temp?.let { tvWeather.text = it }
            }
        }
    }

    private fun playVoxAnimation() {
        val handler = Handler(Looper.getMainLooper())

        voxEmote.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start()

        voxEmote.setImageResource(R.drawable.vox_icon_neutral)

        handler.postDelayed({ voxEmote.setImageResource(R.drawable.vox_icon) }, 200)
        handler.postDelayed({ voxEmote.setImageResource(R.drawable.vox_icon_neutral) }, 400)
        handler.postDelayed({ voxEmote.setImageResource(R.drawable.vox_icon_bad) }, 600)

        handler.postDelayed({
            voxEmote.setImageResource(R.drawable.vox_icon_neutral)
            voxEmote.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }, 1200)
    }

    private fun showVoxPref() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.vox_pref_popup)

        val voxSwitch = dialog.findViewById<Switch>(R.id.switch_vox)
        voxSwitch.isChecked = VoxPrefs.isEnabled(this)

        voxSwitch.setOnCheckedChangeListener { _, isChecked ->
            VoxPrefs.setEnabled(this, isChecked)

            if (isChecked) startService(Intent(this, VoxService::class.java))
            else stopService(Intent(this, VoxService::class.java))
        }

        dialog.show()
    }
    private fun bindUserStaticInfo() {

        val user = FirebaseAuth.getInstance().currentUser ?: return
        tvEmail.text = user.email ?: "No email"

        UserRepository.ensureUserDocument()

        UserRepository.getProfile { name, avatarIndex, code ->
            tvUsername.text = "Name: ${name ?: "User"}"
            tvCode.text = "CODE: ${code ?: "---"}"

            val avatars = listOf(
                R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4,
                R.drawable.avatar5, R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8
            )

            avatarIndex?.let { if (it in avatars.indices) avatar.setImageResource(avatars[it]) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        taskListener?.remove()
        clockManager.stop()
    }
}
