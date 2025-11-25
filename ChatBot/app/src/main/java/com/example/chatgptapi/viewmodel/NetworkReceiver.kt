package com.example.chatgptapi.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast

class NetworkReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        if (isConnected) {
            Toast.makeText(context, "Đã kết nối internet thành công", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Vui lòng kiểm tra kết nối internet!", Toast.LENGTH_SHORT).show()
        }
    }
}