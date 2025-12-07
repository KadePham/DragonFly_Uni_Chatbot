package com.example.chatgptapi.view

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.chatgptapi.R

class SectionChungFragment : Fragment() {

    private lateinit var spTheme: Spinner
    private lateinit var radioAccent: RadioGroup
    private lateinit var rAccentDefault: RadioButton
    private lateinit var rAccentBlue: RadioButton
    private lateinit var rAccentRed: RadioButton

    private val PREF_THEME = "pref_theme"
    private val PREF_ACCENT = "pref_accent"

    private var isUserInitiated = true  // Flag to prevent recreation during preference loading

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_chung, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spTheme = view.findViewById(R.id.spTheme)
        radioAccent = view.findViewById(R.id.radioAccent)
        rAccentDefault = view.findViewById(R.id.rAccentDefault)
        rAccentBlue = view.findViewById(R.id.rAccentBlue)
        rAccentRed = view.findViewById(R.id.rAccentRed)

        setupThemeSpinner()
        setupAccentRadioGroup()
        loadPreferences()
    }

    private fun setupThemeSpinner() {
        spTheme.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Only handle user-initiated changes, not programmatic ones
                if (!isUserInitiated) return

                val theme = when (position) {
                    0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM  // Tự động
                    1 -> AppCompatDelegate.MODE_NIGHT_NO             // Sáng
                    2 -> AppCompatDelegate.MODE_NIGHT_YES            // Tối
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(theme)
                saveThemePreference(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupAccentRadioGroup() {
        radioAccent.setOnCheckedChangeListener { group, checkedId ->
            val accentColor = when (checkedId) {
                R.id.rAccentDefault -> 0  // Mặc định
                R.id.rAccentBlue -> 1     // Xanh
                R.id.rAccentRed -> 2      // Đỏ
                else -> 0
            }
            saveAccentPreference(accentColor)
            applyAccentColor(accentColor)
        }
    }

    private fun applyAccentColor(accentColor: Int) {
        // Save color preference and recreate activity to apply changes
        when (accentColor) {
            0 -> {
                // Mặc định
            }
            1 -> {
                // Xanh
            }
            2 -> {
                // Đỏ
            }
        }
        // Recreate activity to apply accent color changes if needed
        // Uncomment if you want to apply immediately
        // requireActivity().recreate()
    }

    private fun loadPreferences() {
        val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Disable listener temporarily to prevent triggering recreate during load
        isUserInitiated = false

        // Load theme preference
        val themePos = sharedPref.getInt(PREF_THEME, 0)
        spTheme.setSelection(themePos)

        // Load accent preference
        val accentPos = sharedPref.getInt(PREF_ACCENT, 0)
        when (accentPos) {
            0 -> rAccentDefault.isChecked = true
            1 -> rAccentBlue.isChecked = true
            2 -> rAccentRed.isChecked = true
        }

        // Re-enable listener for user interactions
        isUserInitiated = true
    }

    private fun saveThemePreference(position: Int) {
        val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(PREF_THEME, position).apply()
    }

    private fun saveAccentPreference(accentColor: Int) {
        val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPref.edit().putInt(PREF_ACCENT, accentColor).apply()
    }
}

