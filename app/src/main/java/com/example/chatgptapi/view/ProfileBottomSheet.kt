package com.example.chatgptapi.view

// Version: 1.3 - Fixed all duplicate declarations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.chatgptapi.R
import com.example.chatgptapi.viewmodel.ChatRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileBottomSheet : BottomSheetDialogFragment() {
    var onLogoutClick: (() -> Unit)? = null
    var onNameUpdated: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtName = view.findViewById<TextView>(R.id.txtName)
        val txtHandle = view.findViewById<TextView>(R.id.txtHandle)

        val itemLogout = view.findViewById<View>(R.id.itemLogout)
        val itemUpgrade = view.findViewById<LinearLayout>(R.id.itemUpgrade)
        val itemPersonal = view.findViewById<LinearLayout>(R.id.itemPersonal)
        val itemSettings = view.findViewById<LinearLayout>(R.id.itemSettings)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName ?: "User"
        val email = currentUser?.email ?: ""
        val emailHandle = if (email.isNotEmpty()) {
            "@" + email.substringBefore("@")
        } else {
            "@user"
        }

        txtName.text = displayName
        txtHandle.text = emailHandle

        txtName.setOnClickListener {
            showEditNameDialog(txtName, displayName)
        }



        itemUpgrade.setOnClickListener {
            val upgradeDialog = UpgradeDialog()
            upgradeDialog.show(parentFragmentManager, "upgrade_dialog")
            dismiss()
        }

        itemPersonal.setOnClickListener {
            dismiss()
        }

        // Admin menu items
        setupAdminMenuItems(view)

        itemSettings.setOnClickListener {
            try {
                val settings = SettingsBottomSheet()
                settings.show(parentFragmentManager, "settings_bottom")
                view.post { dismiss() }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Không thể mở Cài đặt: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val itemHelp = view.findViewById<View>(R.id.itemHelp)
        itemHelp?.setOnClickListener {
            Log.d("ProfileBottomSheet", "Opening HelpActivity")
            val intent = Intent(activity, HelpActivity::class.java)
            activity?.startActivity(intent)
            dismiss()
        }

        itemLogout.setOnClickListener {
            dismiss()
            onLogoutClick?.invoke()
        }
    }

    private fun setupAdminMenuItems(view: View) {
        val itemAdminInbox = view.findViewById<LinearLayout>(R.id.itemAdminInbox)
        val itemAdminDashboard = view.findViewById<LinearLayout>(R.id.itemAdminDashboard)
        val itemAdminSettings = view.findViewById<LinearLayout>(R.id.itemAdminSettings)

        if (itemAdminInbox == null || itemAdminDashboard == null || itemAdminSettings == null) {
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val repo = ChatRepository()
                val isAdmin = repo.isAdmin()
                withContext(Dispatchers.Main) {
                    if (isAdmin) {
                        itemAdminInbox.visibility = View.VISIBLE
                        itemAdminDashboard.visibility = View.VISIBLE
                        itemAdminSettings.visibility = View.VISIBLE

                        itemAdminInbox.setOnClickListener {
                            Log.d("ProfileBottomSheet", "Opening AdminInboxActivity")
                            val intent = Intent(activity, AdminInboxActivity::class.java)
                            activity?.startActivity(intent)
                            dismiss()
                        }

                        itemAdminDashboard.setOnClickListener {
                            Log.d("ProfileBottomSheet", "Opening AdminDashboardActivity")
                            Toast.makeText(requireContext(), "Select user from Inbox first", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }

                        itemAdminSettings.setOnClickListener {
                            Log.d("ProfileBottomSheet", "Opening AdminSettingsActivity")
                            val intent = Intent(activity, AdminSettingsActivity::class.java)
                            activity?.startActivity(intent)
                            dismiss()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileBottomSheet", "Error checking admin status: ${e.message}")
            }
        }
    }

    private fun showEditNameDialog(txtName: TextView, currentName: String) {
        val input = EditText(requireContext())
        input.setText(currentName)

        AlertDialog.Builder(requireContext())
            .setTitle("Sửa tên")
            .setView(input)
            .setPositiveButton("Lưu") { dialog, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateUserDisplayName(newName, txtName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateUserDisplayName(newName: String, txtName: TextView) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            it.updateProfile(profileUpdate)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.collection("users").document(it.uid)
                            .update(mapOf("displayName" to newName))
                            .addOnSuccessListener {
                                txtName.text = newName
                                Toast.makeText(requireContext(), "Cập nhật tên thành công", Toast.LENGTH_SHORT).show()
                                onNameUpdated?.invoke()
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProfileBottomSheet", "Error updating displayName: ${e.message}")
                                Toast.makeText(requireContext(), "Cập nhật tên thất bại", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Cập nhật tên thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}


