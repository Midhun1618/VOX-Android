package com.voxcom.vox

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 🔁 Auto-login
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)

        val signInButton = findViewById<TextView>(R.id.btnGoogleSignIn)

        signInButton.setOnClickListener {
            playClickSound()
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )

                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    firebaseAuthWithGoogle(googleCredential)
                } else {
                    Toast.makeText(this@LoginActivity, "Unexpected credential type", Toast.LENGTH_LONG).show()
                }


            } catch (e: GetCredentialException) {
                Toast.makeText(this@LoginActivity, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(credential: GoogleIdTokenCredential) {
        val firebaseCredential =
            GoogleAuthProvider.getCredential(credential.idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                val user = auth.currentUser
                Toast.makeText(this, "Firebase auth SUCCESS", Toast.LENGTH_SHORT).show()
                println("🔥 Firebase user UID = ${user?.uid}")
                routeAfterLogin(user?.email ?: "")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Firebase auth FAILED: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
    }


    private fun routeAfterLogin(email: String) {
        val uid = auth.currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        println("📡 Checking Firestore user document for $uid")

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                println("📄 Firestore doc exists = ${doc.exists()}")
                if (doc.exists() && doc.contains("username")) {
                    goToMain()
                } else {
                    val intent = Intent(this, ProfileSettingsActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Firestore FAILED: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
    }


    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun playClickSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.onclick01_sfx)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }
}
