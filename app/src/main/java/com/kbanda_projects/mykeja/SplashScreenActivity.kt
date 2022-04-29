package com.kbanda_projects.mykeja

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kbanda_projects.mykeja.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REQUEST_CODE_SIGN_IN = 0

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        binding
                .buttonSignInWithGoogle
                .setOnClickListener {
                    val options = GoogleSignInOptions
                            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.weclient_id))
                            .requestEmail()
//                            .requestProfile()
                            .build()
                    val signInClient = GoogleSignIn
                            .getClient(this, options)
                    signInClient
                            .signInIntent
                            .also {
                                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
                            }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .result

            account?.let {
                googleAuthForFirebase(it) //Tells firebase how to interpret the account
            }
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null) //Get credentials from account
        CoroutineScope(Dispatchers.IO)
                .launch {
                    try {
                        firebaseAuth
                                .signInWithCredential(credentials)
                                .await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SplashScreenActivity, "Login successful", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SplashScreenActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
    }
}

