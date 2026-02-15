package com.voxcom.vox.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.voxcom.vox.R
import com.voxcom.vox.data.TaskManager
import com.voxcom.vox.data.repository.ClipboardRepository
import com.voxcom.vox.data.repository.TaskRepository
import com.voxcom.vox.data.repository.UserRepository
import com.voxcom.vox.data.repository.WeatherRepository
import com.voxcom.vox.service.VoxService
import com.voxcom.vox.system.*
import com.voxcom.vox.ui.fragments.ActiveTasksFragment
import com.voxcom.vox.ui.fragments.PastTasksFragment
import com.voxcom.vox.voice.VoxAssistantManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var voxManager: VoxAssistantManager
    private val clockManager = ClockManager()
    private lateinit var etTask: EditText
    private lateinit var btnAdd: TextView
    private lateinit var voxEmote: ImageView
    private lateinit var tvStats: TextView
    private lateinit var tvWeather: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvAmPm: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvDescipline: TextView
    private lateinit var avatar: ImageView
    private var taskListener: ListenerRegistration? = null
    private var clipboardListener: ListenerRegistration? = null
    private lateinit var fragActive: TextView
    private lateinit var fragPast: TextView
    private lateinit var fragReminder: TextView
    private lateinit var tabs: List<TextView>
    private lateinit var tvLatestFromPc: TextView
    private lateinit var btnCopyToPhone: TextView
    private lateinit var etManualPaste: EditText
    private lateinit var btnUploadClipboard: TextView
    private lateinit var backClipboard: TextView


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.all { it }) initializeApp()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VoxNotification.createChannel(this)


        bindViews()
        clockManager.start(tvTime, tvAmPm)

        tabs = listOf(fragActive, fragPast, fragReminder)

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
        startTaskSync()
        loadWeather()
        setupListeners()
        startClipboardLiveSync()


        openFragment(ActiveTasksFragment())
    }

    private fun startTaskSync() {

        taskListener = TaskRepository.listenTasks { tasks ->
            TaskManager.update(tasks)
        }
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
        fragActive = findViewById(R.id.btnActive)
        fragPast = findViewById(R.id.btnPast)
        fragReminder = findViewById(R.id.btnReminder)

        tvLatestFromPc = findViewById(R.id.tvLatestFromPc)
        btnCopyToPhone = findViewById(R.id.btnCopyToPhone)
        etManualPaste = findViewById(R.id.etManualPaste)
        btnUploadClipboard = findViewById(R.id.btnUploadClipboard)
        backClipboard = findViewById(R.id.backClipboard)

        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        tvCode = findViewById(R.id.codeTv)
        avatar = findViewById(R.id.myAvatar)
    }


    private fun observeAppState() {

        TaskManager.observe {

            val (total, done, missed) = TaskManager.stats()

            tvStats.text = """
                ADDED: $total
                DONE: $done
                MISSED: $missed
            """.trimIndent()

            val discipline = if (total == 0) 0 else ((done.toFloat() / total) * 100).toInt()
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

        fragActive.setOnClickListener {
            openFragment(ActiveTasksFragment())
            selectTab(0)
        }

        fragPast.setOnClickListener {
            openFragment(PastTasksFragment())
            selectTab(1)
        }

        fragReminder.setOnClickListener {
            openFragment(PastTasksFragment())
            selectTab(2)
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
        btnCopyToPhone.setOnClickListener {

            val text = tvLatestFromPc.text.toString()
            if (text.isBlank()) return@setOnClickListener

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("VOX", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied to phone clipboard", Toast.LENGTH_SHORT).show()
        }

        tvLatestFromPc.setOnClickListener {

            tvLatestFromPc.visibility = View.GONE
            btnCopyToPhone.visibility = View.GONE

            etManualPaste.visibility = View.VISIBLE
            btnUploadClipboard.visibility = View.VISIBLE
            backClipboard.visibility = View.VISIBLE

            etManualPaste.requestFocus()
        }
        backClipboard.setOnClickListener {

            etManualPaste.visibility = View.GONE
            btnUploadClipboard.visibility = View.GONE
            backClipboard.visibility = View.GONE

            tvLatestFromPc.visibility = View.VISIBLE
            btnCopyToPhone.visibility = View.VISIBLE

            etManualPaste.requestFocus()
        }

        btnUploadClipboard.setOnClickListener {

            val text = etManualPaste.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            ClipboardRepository.push(text, "phone")

            Toast.makeText(this, "Uploaded to PC", Toast.LENGTH_SHORT).show()

            // Exit edit mode
            etManualPaste.text.clear()
            etManualPaste.visibility = View.GONE
            btnUploadClipboard.visibility = View.GONE

            tvLatestFromPc.visibility = View.VISIBLE
            btnCopyToPhone.visibility = View.VISIBLE
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

    private fun startClipboardLiveSync() {

        clipboardListener = ClipboardRepository.listen { content, device, time ->

            if (device == "phone") return@listen

            runOnUiThread {
                tvLatestFromPc.text = content
            }
        }
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

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun selectTab(index: Int) {

        tabs.forEachIndexed { i, tab ->

            if (i == index) {
                tab.setBackgroundColor(getColor(R.color.yellow_main))
            } else {
                tab.setBackgroundColor(getColor(R.color.yellow_main30))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        taskListener?.remove()
        clipboardListener?.remove()
        clockManager.stop()
    }

}