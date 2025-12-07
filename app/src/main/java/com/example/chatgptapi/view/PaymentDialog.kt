package com.example.chatgptapi.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chatgptapi.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentDialog(private val planName: String, private val price: String) :
    BottomSheetDialogFragment() {

    private var onPaymentSuccess: (() -> Unit)? = null

    fun setOnPaymentSuccess(callback: () -> Unit) {
        onPaymentSuccess = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edtCardNumber = view.findViewById<EditText>(R.id.edtCardNumber)
        val edtCardholderName = view.findViewById<EditText>(R.id.edtCardholderName)
        val edtExpiryDate = view.findViewById<EditText>(R.id.edtExpiryDate)
        val edtCVV = view.findViewById<EditText>(R.id.edtCVV)
        val btnPay = view.findViewById<Button>(R.id.btnPay)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // Format card number: 1234 5678 9012 3456
        edtCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(" ", "")
                if (input.length > 16) {
                    edtCardNumber.setText(input.substring(0, 16))
                    edtCardNumber.setSelection(16)
                    return
                }
                val formatted = StringBuilder()
                for (i in input.indices) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ")
                    }
                    formatted.append(input[i])
                }
                if (formatted.toString() != s.toString()) {
                    edtCardNumber.setText(formatted.toString())
                    edtCardNumber.setSelection(formatted.length)
                }
            }
        })

        // Format expiry date: MM/YY
        edtExpiryDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace("/", "")
                if (input.length > 4) {
                    edtExpiryDate.setText(input.substring(0, 4))
                    edtExpiryDate.setSelection(4)
                    return
                }
                val formatted = if (input.length >= 2) {
                    input.substring(0, 2) + "/" + input.substring(2)
                } else {
                    input
                }
                if (formatted != s.toString()) {
                    edtExpiryDate.setText(formatted)
                    edtExpiryDate.setSelection(formatted.length)
                }
            }
        })

        // Pay button
        btnPay.setOnClickListener {
            val cardNumber = edtCardNumber.text.toString().replace(" ", "")
            val cardholderName = edtCardholderName.text.toString()
            val expiryDate = edtExpiryDate.text.toString()
            val cvv = edtCVV.text.toString()

            // Validation
            if (cardNumber.isEmpty() || cardNumber.length != 16) {
                Toast.makeText(requireContext(), "Số thẻ phải có 16 chữ số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardholderName.isEmpty()) {
                Toast.makeText(requireContext(), "Tên chủ thẻ không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expiryDate.isEmpty() || expiryDate.length != 5) {
                Toast.makeText(requireContext(), "Ngày hết hạn phải ở dạng MM/YY", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvv.isEmpty() || cvv.length < 3) {
                Toast.makeText(requireContext(), "CVV phải có 3-4 chữ số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulate payment
            Toast.makeText(
                requireContext(),
                "✅ Thanh toán $price cho gói $planName thành công!",
                Toast.LENGTH_LONG
            ).show()

            onPaymentSuccess?.invoke()
            dismiss()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}

