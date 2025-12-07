package com.example.chatgptapi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.example.chatgptapi.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UpgradeDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_upgrade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val planFree = view.findViewById<LinearLayout>(R.id.planFree)
        val planPro = view.findViewById<LinearLayout>(R.id.planPro)
        val planPremium = view.findViewById<LinearLayout>(R.id.planPremium)
        val btnUpgrade = view.findViewById<Button>(R.id.btnUpgrade)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        var selectedPlan = "free"

        // Plan selection
        planFree.setOnClickListener {
            selectedPlan = "free"
            updateSelection(planFree, planPro, planPremium)
            btnUpgrade.text = "Đang dùng"
            btnUpgrade.isEnabled = false
        }

        planPro.setOnClickListener {
            selectedPlan = "pro"
            updateSelection(planPro, planFree, planPremium)
            btnUpgrade.text = "Nâng cấp lên Pro"
            btnUpgrade.isEnabled = true
        }

        planPremium.setOnClickListener {
            selectedPlan = "premium"
            updateSelection(planPremium, planFree, planPro)
            btnUpgrade.text = "Nâng cấp lên Premium"
            btnUpgrade.isEnabled = true
        }

        // Upgrade button
        btnUpgrade.setOnClickListener {
            if (selectedPlan != "free") {
                // Get plan details
                val (planName, price) = when (selectedPlan) {
                    "pro" -> "Pro" to "499K/tháng"
                    "premium" -> "Premium" to "999K/tháng"
                    else -> "Free" to "Miễn phí"
                }

                // Open payment dialog
                val paymentDialog = PaymentDialog(planName, price)
                paymentDialog.setOnPaymentSuccess {
                    Toast.makeText(
                        requireContext(),
                        "Nâng cấp lên $planName thành công!",
                        Toast.LENGTH_LONG
                    ).show()
                    dismiss()
                }
                paymentDialog.show(parentFragmentManager, "payment_dialog")
            }
        }

        // Close button
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSelection(
        selected: LinearLayout,
        other1: LinearLayout,
        other2: LinearLayout
    ) {
        selected.alpha = 1f
        other1.alpha = 0.6f
        other2.alpha = 0.6f
    }
}

