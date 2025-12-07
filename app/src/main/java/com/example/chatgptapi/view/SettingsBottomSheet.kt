package com.example.chatgptapi.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.chatgptapi.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tabGeneral: LinearLayout
    private lateinit var tabNotifications: LinearLayout
    private lateinit var tabAccount: LinearLayout
    private lateinit var contentContainer: ViewGroup
    private lateinit var btnCloseSettings: ImageButton

    private var currentTab = "chung"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Light_BottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_settings_bottom_sheet, container, false)

        // Initialize views
        tabGeneral = v.findViewById(R.id.tabGeneral)
        tabNotifications = v.findViewById(R.id.tabNotifications)
        tabAccount = v.findViewById(R.id.tabAccount)
        contentContainer = v.findViewById(R.id.contentContainer)
        btnCloseSettings = v.findViewById(R.id.btnCloseSettings)

        // Setup tab click listeners
        tabGeneral.setOnClickListener {
            switchTab("chung", tabGeneral)
        }

        tabNotifications.setOnClickListener {
            switchTab("thongbao", tabNotifications)
        }

        tabAccount.setOnClickListener {
            switchTab("taikhoan", tabAccount)
        }

        // Close button
        btnCloseSettings.setOnClickListener {
            dismiss()
        }

        // Show default section
        showSection("chung")
        updateTabColors()
        return v
    }

    private fun switchTab(tabId: String, selectedTab: LinearLayout) {
        if (currentTab == tabId) return

        currentTab = tabId

        // Update tab colors
        updateTabColors()

        // Show section
        showSection(tabId)
    }

    private fun updateTabColors() {
        val tabViews = listOf(
            tabGeneral to "chung",
            tabNotifications to "thongbao",
            tabAccount to "taikhoan"
        )

        tabViews.forEach { (tab, id) ->
            val isActive = currentTab == id
            val textView = tab.getChildAt(1) as? android.widget.TextView
            val iconView = tab.getChildAt(0) as? android.widget.ImageView

            if (isActive) {
                // Active state
                textView?.setTextColor(resources.getColor(R.color.text_primary, null))
                textView?.setTypeface(null, android.graphics.Typeface.BOLD)
                iconView?.setColorFilter(resources.getColor(R.color.text_primary, null))
                tab.setBackgroundColor(resources.getColor(R.color.header_bg, null))
            } else {
                // Inactive state
                textView?.setTextColor(resources.getColor(R.color.text_secondary, null))
                textView?.setTypeface(null, android.graphics.Typeface.NORMAL)
                iconView?.setColorFilter(resources.getColor(R.color.text_secondary, null))
                tab.setBackgroundColor(resources.getColor(R.color.sidebar_bg, null))
            }
        }
    }

    private fun showSection(id: String) {
        val fragment = when (id) {
            "chung" -> SectionChungFragment()
            "thongbao" -> SectionNotificationsFragment()
            "taikhoan" -> SectionAccountFragment()
            else -> SectionChungFragment()
        }

        try {
            childFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, fragment, id)
                .commitNow()
        } catch (e: Exception) {
            Log.e("SettingsBottomSheet", "Error showing section: ${e.message}", e)
        }
    }
}
