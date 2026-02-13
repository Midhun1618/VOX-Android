package com.voxcom.vox.ui.profile

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voxcom.vox.R
import com.voxcom.vox.ui.main.MainActivity

class ProfileSettingsActivity : AppCompatActivity() {

    private var selectedAvatar = 0
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val user = FirebaseAuth.getInstance().currentUser!!
        uid = user.uid
        db = FirebaseFirestore.getInstance()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnSave = findViewById<TextView>(R.id.btnSave)
        val avatar = findViewById<ImageView>(R.id.avatar)

        val avatarDrawables = listOf(
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5,
            R.drawable.avatar6,
            R.drawable.avatar7,
            R.drawable.avatar8,
        )

        val avatarViews = listOf(
            findViewById<ImageView>(R.id.avatar0),
            findViewById<ImageView>(R.id.avatar1),
            findViewById<ImageView>(R.id.avatar2),
            findViewById<ImageView>(R.id.avatar3),
            findViewById<ImageView>(R.id.avatar4),
            findViewById<ImageView>(R.id.avatar5),
            findViewById<ImageView>(R.id.avatar6),
            findViewById<ImageView>(R.id.avatar7),
        )

        avatarViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                playClickSound()
                selectedAvatar = index

                avatarViews.forEach {
                    it.setBackgroundResource(R.drawable.container_m)
                }

                imageView.setBackgroundResource(R.drawable.container_x)

                avatar.setImageResource(avatarDrawables[index])
            }
        }

        btnSave.setOnClickListener {
            playClickSound()

            val username = etUsername.text.toString().trim()
            if (username.isEmpty()) return@setOnClickListener

            val userRef = db.collection("users").document(uid)

            val data = hashMapOf(
                "email" to FirebaseAuth.getInstance().currentUser?.email,
                "username" to username,
                "avatarIndex" to selectedAvatar,
                "accessCode" to generateAccessCode(),
                "totalTask" to 0,
                "completedTask" to 0,
                "createdAt" to Timestamp.now()
            )

            userRef.set(data)
                .addOnSuccessListener {
                    // ✅ User document is now COMPLETE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }

    }
    private fun playClickSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.onclick01_sfx)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }
    private fun generateAccessCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

}