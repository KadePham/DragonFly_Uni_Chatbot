package com.example.chatgptapi.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatgptapi.R
import com.google.android.material.textfield.TextInputEditText

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ForgotPasswordActivity : AppCompatActivity() {

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val edtEmail = findViewById<TextInputEditText>(R.id.edtEmailReset)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val txtThongBao = findViewById<TextView>(R.id.txtThongBao)

        btnReset.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Nhập email đã đăng ký", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gửi link reset mật khẩu
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set text theo yêu cầu
                        txtThongBao.text = "Đã gửi link đổi mật khẩu thành công. Vui lòng kiểm tra email."
                        Toast.makeText(this, "Đã gửi link đổi mật khẩu", Toast.LENGTH_SHORT).show()
                    } else {
                        txtThongBao.text = "Gửi thất bại: ${task.exception?.message}"
                        Toast.makeText(this, "Gửi thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
