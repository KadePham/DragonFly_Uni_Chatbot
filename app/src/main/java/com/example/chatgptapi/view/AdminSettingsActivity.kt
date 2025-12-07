package com.example.chatgptapi.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chatgptapi.databinding.ActivityAdminSettingsBinding
import com.example.chatgptapi.viewmodel.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AdminSettingsActivity - Manually set user roles
 * Admin-only function to manage user permissions
 */
class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private val repo = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if current user is admin
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val isAdmin = repo.isAdmin()
                withContext(Dispatchers.Main) {
                    if (!isAdmin) {
                        Toast.makeText(
                            this@AdminSettingsActivity,
                            "Bạn không phải admin - không có quyền truy cập",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                        return@withContext
                    }
                    setupUI()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminSettingsActivity,
                        "Lỗi kiểm tra quyền: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun setupUI() {
        // Get Role button
        binding.btnGetRole.setOnClickListener {
            val email = binding.edtUserEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Nhập email user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            getUserRoleByEmail(email)
        }

        // Set Role button
        binding.btnSetRole.setOnClickListener {
            val email = binding.edtUserEmail.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString().lowercase()

            if (email.isEmpty()) {
                Toast.makeText(this, "Nhập email user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setUserRoleByEmail(email, role)
        }
    }

    private fun getUserRoleByEmail(email: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                @Suppress("UNRESOLVED_REFERENCE")
                val role = repo.getUserRoleByEmail(email)
                withContext(Dispatchers.Main) {
                    binding.txtResult.text = "Email: $email\nRole: $role"
                    Log.d("AdminSettings", "Got role for $email: $role")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Lỗi: ${e.message}"
                    binding.txtResult.text = errorMsg
                    Toast.makeText(this@AdminSettingsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("AdminSettings", "Error getting role: ${e.message}", e)
                }
            }
        }
    }

    private fun setUserRoleByEmail(email: String, role: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("AdminSettings", "Setting role for $email to $role")
                @Suppress("UNRESOLVED_REFERENCE")
                repo.setUserRoleByEmail(email, role)
                withContext(Dispatchers.Main) {
                    val successMsg = "✅ Đã set role!\nEmail: $email\nRole: $role"
                    binding.txtResult.text = successMsg
                    Toast.makeText(this@AdminSettingsActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    Log.d("AdminSettings", "Successfully set role")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "❌ Lỗi: ${e.message}"
                    binding.txtResult.text = errorMsg
                    Toast.makeText(this@AdminSettingsActivity, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("AdminSettings", "Error setting role: ${e.message}", e)
                }
            }
        }
    }
}

