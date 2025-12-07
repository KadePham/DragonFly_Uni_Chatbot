package com.example.chatgptapi.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chatgptapi.R
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val repo = ChatRepository()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtEmail = findViewById<TextInputEditText>(R.id.edtUsername)
        val edtPass = findViewById<TextInputEditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<ImageButton>(R.id.btnGoogle)
        val btnAnonymous = findViewById<ImageButton>(R.id.btnAnonymous)
        val txtSignUp = findViewById<TextView>(R.id.txtSignUp)
        val txtForgot = findViewById<TextView>(R.id.txtForgot)

        // Setup Google Sign-In
        setupGoogleSignIn()

        auth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                repo.ensureUserExists()
                                android.util.Log.d("LoginActivity", "User ensured, role setup complete")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnAnonymous.setOnClickListener {
            openFacebook()
        }

        txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        txtForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(Exception::class.java)

                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken ?: return)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Google sign in failed: ${e.message}", e)
                Toast.makeText(this, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.ensureUserExists()
                            android.util.Log.d("LoginActivity", "Google user ensured, role setup complete")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    Toast.makeText(this, "✅ Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Xác thực Google thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun openFacebook() {
        try {
            // Thử mở Facebook app
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.facebook.com")
            intent.setPackage("com.facebook.katana")
            startActivity(intent)
        } catch (e: Exception) {
            // Nếu không có Facebook app, mở bằng browser
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.facebook.com")
            startActivity(intent)
            Toast.makeText(this, "Đang mở Facebook", Toast.LENGTH_SHORT).show()
        }
    }
}
