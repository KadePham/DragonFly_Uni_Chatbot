package com.example.chatgptapi.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chatgptapi.databinding.ActivityQuickAdminBinding
import com.example.chatgptapi.viewmodel.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * QuickAdminActivity - Simple way to set role to admin
 * Just enter your email and click button
 */
class QuickAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickAdminBinding
    private val repo = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetAdmin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Gọi hàm setUserRoleByEmail để set role = admin
                    @Suppress("UNRESOLVED_REFERENCE")
                    repo.setUserRoleByEmail(email, "admin")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@QuickAdminActivity,
                            "✅ Set role admin cho $email thành công!",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.edtEmail.text.clear()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@QuickAdminActivity,
                            "❌ Lỗi: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}

