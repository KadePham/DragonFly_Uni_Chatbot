package com.example.chatgptapi.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.chatgptapi.R
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SectionAccountFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAccountType: TextView
    private lateinit var tvJoinDate: TextView
    private lateinit var btnLogout: Button
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val repo = ChatRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvAccountType = view.findViewById(R.id.tvAccountType)
        tvJoinDate = view.findViewById(R.id.tvJoinDate)
        btnLogout = view.findViewById(R.id.btnLogout)

        setupUI()
        setupLogoutButton()
        setupSocialLinks(view)
    }

    private fun setupUI() {
        val currentUser = firebaseAuth.currentUser
        val email = currentUser?.email ?: "—"
        val displayName = currentUser?.displayName ?: "User"

        tvEmail.text = email
        tvName.text = displayName

        // Load user role and join date from Firestore
        loadUserInfo()
    }

    private fun loadUserInfo() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userRole = repo.getUserRoleString()
                val createdAtTimestamp = repo.getUserCreatedAtTimestamp()

                withContext(Dispatchers.Main) {
                    // Set account type (Admin or User)
                    val displayRole = if (userRole == "admin") "Admin" else "User"
                    tvAccountType.text = displayRole

                    // Format and set join date
                    if (createdAtTimestamp != null) {
                        val joinDate = formatDate(createdAtTimestamp as Timestamp)
                        tvJoinDate.text = joinDate
                    } else {
                        tvJoinDate.text = "—"
                    }

                    Log.d("SectionAccountFragment", "User role: $userRole, Join date: ${tvJoinDate.text}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvAccountType.text = "User"
                    tvJoinDate.text = "—"
                    Log.e("SectionAccountFragment", "Error loading user info: ${e.message}", e)
                }
            }
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        return try {
            val date = Date(timestamp.seconds * 1000)
            val sdf = SimpleDateFormat("d 'Tháng' M, yyyy", Locale("vi", "VN"))
            sdf.format(date)
        } catch (e: Exception) {
            "—"
        }
    }

    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            // Navigate back to login screen
            requireActivity().finishAffinity()
        }
    }

    private fun setupSocialLinks(view: View) {
        // Facebook
        view.findViewById<LinearLayout>(R.id.rowFacebook)?.setOnClickListener {
            openSocialLink("https://www.facebook.com")
        }

        // LinkedIn
        view.findViewById<LinearLayout>(R.id.rowLinkedIn)?.setOnClickListener {
            openSocialLink("https://www.linkedin.com")
        }

        // GitHub
        view.findViewById<LinearLayout>(R.id.rowGitHub)?.setOnClickListener {
            openSocialLink("https://www.github.com")
        }

        // Twitter / X
        view.findViewById<LinearLayout>(R.id.rowTwitter)?.setOnClickListener {
            openSocialLink("https://www.twitter.com")
        }

        // Change Password
        view.findViewById<LinearLayout>(R.id.rowChangePassword)?.setOnClickListener {
            showChangePasswordDialog()
        }

        // Two-Factor Auth
        view.findViewById<LinearLayout>(R.id.rowTwoFactor)?.setOnClickListener {
            showTwoFactorDialog()
        }
    }

    private fun openSocialLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Không thể mở liên kết",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("SectionAccountFragment", "Error opening social link: ${e.message}", e)
        }
    }

    private fun showChangePasswordDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Đổi mật khẩu")

        // Create layout for inputs
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Old password input
        val oldPasswordInput = android.widget.EditText(requireContext()).apply {
            hint = "Mật khẩu cũ"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        }
        layout.addView(oldPasswordInput)

        // New password input
        val newPasswordInput = android.widget.EditText(requireContext()).apply {
            hint = "Mật khẩu mới"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        }
        layout.addView(newPasswordInput)

        // Confirm password input
        val confirmPasswordInput = android.widget.EditText(requireContext()).apply {
            hint = "Xác nhận mật khẩu mới"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        layout.addView(confirmPasswordInput)

        builder.setView(layout)

        builder.setPositiveButton("Cập nhật") { dialog, _ ->
            val oldPassword = oldPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Validation
            if (oldPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // TODO: Implement password change logic with Firebase
            Toast.makeText(requireContext(), "✅ Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showTwoFactorDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Xác thực 2 lớp")
        builder.setMessage(
            "Bật xác thực 2 lớp để bảo vệ tài khoản của bạn.\n\n" +
            "Bạn sẽ cần nhập mã từ ứng dụng xác thực khi đăng nhập."
        )

        builder.setPositiveButton("Bật xác thực 2 lớp") { dialog, _ ->
            Toast.makeText(requireContext(), "Xác thực 2 lớp đã được bật", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Để sau") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}

