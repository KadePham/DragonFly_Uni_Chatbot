<!-- Hướng dẫn cấu hình Accent Colors trong styles.xml -->

<!-- File: res/values/styles.xml hoặc res/values/themes.xml -->

<!-- Base AppTheme -->
<resources>
    <!-- Default Theme -->
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.DarkActionBar">
        <item name="colorPrimary">@color/primary_default</item>
        <item name="colorPrimaryVariant">@color/primary_default_dark</item>
        <item name="colorSecondary">@color/accent_default</item>
        <item name="colorSecondaryVariant">@color/accent_default_dark</item>
    </style>

    <!-- Blue Accent Theme -->
    <style name="AppTheme.BlueAccent" parent="AppTheme">
        <item name="colorSecondary">@color/accent_blue</item>
        <item name="colorSecondaryVariant">@color/accent_blue_dark</item>
    </style>

    <!-- Red Accent Theme -->
    <style name="AppTheme.RedAccent" parent="AppTheme">
        <item name="colorSecondary">@color/accent_red</item>
        <item name="colorSecondaryVariant">@color/accent_red_dark</item>
    </style>

    <!-- Dark Mode Variants -->
    <style name="AppTheme.Dark" parent="Theme.MaterialComponents.NoActionBar">
        <item name="colorPrimary">@color/primary_dark</item>
        <item name="colorSecondary">@color/accent_default</item>
    </style>

    <style name="AppTheme.Dark.BlueAccent" parent="AppTheme.Dark">
        <item name="colorSecondary">@color/accent_blue</item>
    </style>

    <style name="AppTheme.Dark.RedAccent" parent="AppTheme.Dark">
        <item name="colorSecondary">@color/accent_red</item>
    </style>
</resources>

<!-- File: res/values/colors.xml -->

<resources>
    <!-- Primary Colors -->
    <color name="primary_default">#2196F3</color>
    <color name="primary_default_dark">#1976D2</color>
    <color name="primary_dark">#121212</color>

    <!-- Accent Colors - Default -->
    <color name="accent_default">#FF5722</color>
    <color name="accent_default_dark">#E64A19</color>

    <!-- Accent Colors - Blue -->
    <color name="accent_blue">#00BCD4</color>
    <color name="accent_blue_dark">#0097A7</color>

    <!-- Accent Colors - Red -->
    <color name="accent_red">#F44336</color>
    <color name="accent_red_dark">#D32F2F</color>

    <!-- Additional Colors -->
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
</resources>

<!-- Cách tích hợp vào SectionChungFragment.kt -->

/*
private fun applyAccentColor(accentColor: Int) {
    val activity = requireActivity()
    when (accentColor) {
        0 -> {
            // Mặc định
            activity.setTheme(R.style.AppTheme)
        }
        1 -> {
            // Xanh
            activity.setTheme(R.style.AppTheme_BlueAccent)
        }
        2 -> {
            // Đỏ
            activity.setTheme(R.style.AppTheme_RedAccent)
        }
    }
    activity.recreate()
}

private fun loadPreferences() {
    val sharedPref = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
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
    
    // Apply saved accent color
    applyAccentColor(accentPos)
}
*/

