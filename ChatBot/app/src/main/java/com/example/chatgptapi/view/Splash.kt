package com.example.chatgptapi.view



import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatgptapi.R
import com.example.chatgptapi.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main3)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val nextScreen = intent.getStringExtra("nextScreen")

        // fake loading 2s
        val progressBar = binding.progressBar
        val txtPercent = binding.txtPercent


        // Giả lập loading 2s (0 → 100%)
        Thread {
            for (i in 0..100) {
                Thread.sleep(15) // 100 * 15ms = 1500ms (1.5s)
                runOnUiThread {
                    progressBar.progress = i
                    txtPercent.text = "$i%"
                }
            }


            runOnUiThread {
                binding.processBar2.visibility = View.VISIBLE

                // Delay thêm 1.5s cho processBar2
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.processBar2.visibility = View.GONE

                    when (nextScreen) {
                        "main" -> startActivity(Intent(this, MainActivity::class.java))
                        else -> startActivity(Intent(this, Login::class.java))
                    }
                    finish()
                }, 1500) // 1,5s
            }
        }.start()

    }
}