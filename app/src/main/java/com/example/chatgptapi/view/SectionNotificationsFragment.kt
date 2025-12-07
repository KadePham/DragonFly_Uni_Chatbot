package com.example.chatgptapi.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.chatgptapi.R

class SectionNotificationsFragment : Fragment() {

    private lateinit var switchPush: Switch
    private val PREF_PUSH_NOTIFICATION = "pref_push_notification"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchPush = view.findViewById(R.id.switchPush)
        setupPushNotificationSwitch()
        loadPreferences()
    }

    private fun setupPushNotificationSwitch() {
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            savePushNotificationPreference(isChecked)
            // Implement push notification enable/disable logic here
            if (isChecked) {
                enablePushNotifications()
            } else {
                disablePushNotifications()
            }
        }
    }

    private fun enablePushNotifications() {
        // TODO: Implement Firebase Cloud Messaging subscription
        // FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")
    }

    private fun disablePushNotifications() {
        // TODO: Implement Firebase Cloud Messaging unsubscription
        // FirebaseMessaging.getInstance().unsubscribeFromTopic("all_notifications")
    }

    private fun loadPreferences() {
        val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isPushEnabled = sharedPref.getBoolean(PREF_PUSH_NOTIFICATION, true)
        switchPush.isChecked = isPushEnabled
    }

    private fun savePushNotificationPreference(isEnabled: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(PREF_PUSH_NOTIFICATION, isEnabled).apply()
    }
}

