package com.example.chatgptapi.view

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatgptapi.R
import com.example.chatgptapi.databinding.ActivityLoginBinding
import com.example.chatgptapi.viewmodel.NetworkReceiver
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var networkReceiver: NetworkReceiver
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.btnLogin.setOnClickListener {
            if (binding.edtUsername.text.isNullOrEmpty()) {
                binding.txtInputUsername.error = "Không được để trống !"
            } else {
                binding.txtInputUsername.error = null
            }


            if (binding.edtPassword.text.isNullOrEmpty()) {
                binding.txtInputPassword.error = "Không được để trống !"
            } else {
                binding.txtInputPassword.error = null
            }
            checkLogin()
        }

        binding.ungHoToi.setOnClickListener {
            val dialogView = LayoutInflater.from(this)
                .inflate(R.layout.qr_bank, null)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()



            alertDialog.show()
        }
    }

    private fun checkLogin() {
        binding.progressBar.visibility = View.VISIBLE
        binding.txtLoading.visibility = View.VISIBLE
        val username = binding.edtUsername.text.toString().trim().lowercase()
        val password = binding.edtPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Không được để trống !", Toast.LENGTH_SHORT).show()
            return
        }

        val email = if (username.contains("@")) username else "$username@gmail.com"

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.txtLoading.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Đăng nhập thành công, xin chào ${username}",
                    Toast.LENGTH_SHORT
                ).show()
                val i = Intent(this, Splash::class.java)
                i.putExtra("nextScreen", "main") // truyền thêm flag
                startActivity(i)
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.txtLoading.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        networkReceiver = NetworkReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }
}