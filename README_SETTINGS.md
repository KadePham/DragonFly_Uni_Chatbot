# 📱 Settings Module - README

## 🎯 Tổng Quan

Module Settings cung cấp giao diện BottomSheet Dialog cho phép người dùng quản lý cài đặt ứng dụng bao gồm:
- **Chung**: Theme, Accent Color
- **Thông báo**: Push Notifications
- **Tài khoản**: Email, Logout

## 🚀 Cách Sử Dụng

### 1. Mở Settings BottomSheet

```kotlin
// Trong Activity hoặc Fragment
val settingsBottomSheet = SettingsBottomSheet()
settingsBottomSheet.show(supportFragmentManager, "SettingsBottomSheet")
```

### 2. Thêm Menu Item

```kotlin
// Trong onCreateOptionsMenu
override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
}

// Trong onOptionsItemSelected
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_settings -> {
            val settingsBottomSheet = SettingsBottomSheet()
            settingsBottomSheet.show(supportFragmentManager, "SettingsBottomSheet")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

### 3. Từ Button

```xml
<!-- activity_main.xml -->
<Button
    android:id="@+id/btnSettings"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Cài đặt"
    android:onClick="openSettings" />
```

```kotlin
// MainActivity.kt
fun openSettings(view: View) {
    val settingsBottomSheet = SettingsBottomSheet()
    settingsBottomSheet.show(supportFragmentManager, "SettingsBottomSheet")
}
```

## 🏗️ Cấu Trúc Thư Mục

```
app/src/main/
├── java/com/example/chatgptapi/view/
│   ├── SettingsBottomSheet.kt          (Parent Fragment - quản lý navigation)
│   ├── SettingsNavAdapter.kt           (RecyclerView Adapter)
│   ├── SectionChungFragment.kt         (Theme & Accent Color)
│   ├── SectionNotificationsFragment.kt (Push Notifications)
│   └── SectionAccountFragment.kt       (Email & Logout)
│
└── res/layout/
    ├── fragment_settings_bottom_sheet.xml  (Main layout)
    ├── item_settings_nav.xml              (Navigation item)
    ├── section_chung.xml                  (Theme settings)
    ├── section_notifications.xml          (Push settings)
    └── section_account.xml                (Account settings)
```

## 🎨 Section Chung

### Chức Năng

#### 1. Theme Selection
- **Tự động**: Theo cài đặt hệ thống
- **Sáng**: Chế độ light
- **Tối**: Chế độ dark

```kotlin
val theme = when (position) {
    0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    1 -> AppCompatDelegate.MODE_NIGHT_NO
    2 -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
AppCompatDelegate.setDefaultNightMode(theme)
```

#### 2. Accent Color
- **Mặc định**: Màu chính của app
- **Xanh**: Custom blue accent
- **Đỏ**: Custom red accent

```kotlin
when (accentColor) {
    0 -> { /* Mặc định */ }
    1 -> { /* Xanh */ }
    2 -> { /* Đỏ */ }
}
saveAccentPreference(accentColor)
```

### SharedPreferences Storage

```kotlin
// Lưu
val sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
sharedPref.edit()
    .putInt("pref_theme", 0)
    .putInt("pref_accent", 0)
    .apply()

// Tải
val theme = sharedPref.getInt("pref_theme", 0)
val accent = sharedPref.getInt("pref_accent", 0)
```

## 🔔 Section Notifications

### Chức Năng

**Push Notification Toggle**
- Bật: Nhận thông báo push từ Firebase Cloud Messaging
- Tắt: Không nhận thông báo

```kotlin
switchPush.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")
    } else {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_notifications")
    }
    savePushNotificationPreference(isChecked)
}
```

### SharedPreferences Storage

```kotlin
// Lưu
val sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
sharedPref.edit()
    .putBoolean("pref_push_notification", true)
    .apply()

// Tải
val isPushEnabled = sharedPref.getBoolean("pref_push_notification", true)
```

## 👤 Section Account

### Chức Năng

#### 1. Display Email
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
val email = currentUser?.email ?: "—"
tvEmail.text = email
```

#### 2. Logout
```kotlin
btnLogout.setOnClickListener {
    FirebaseAuth.getInstance().signOut()
    requireActivity().finishAffinity() // Xoá stack & quay về LoginActivity
}
```

## 🔧 Configuration

### Theme Attributes (colors.xml)

```xml
<resources>
    <color name="accent_default">#FF5722</color>
    <color name="accent_blue">#00BCD4</color>
    <color name="accent_red">#F44336</color>
</resources>
```

### String Array (strings.xml)

```xml
<string-array name="pref_theme_values">
    <item>Tự động</item>
    <item>Sáng</item>
    <item>Tối</item>
</string-array>
```

## 🔌 Integration

### Với MainActivity

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load theme preferences
        val sharedPref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val themePos = sharedPref.getInt("pref_theme", 0)
        
        val theme = when (themePos) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(theme)
        
        setContentView(R.layout.activity_main)
    }
}
```

### Với Firebase Cloud Messaging

```kotlin
class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Kiểm tra user preference
        val sharedPref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isPushEnabled = sharedPref.getBoolean("pref_push_notification", true)
        
        if (isPushEnabled) {
            // Hiển thị notification
            val notification = remoteMessage.notification
            if (notification != null) {
                sendNotification(notification.title, notification.body)
            }
        }
    }
    
    private fun sendNotification(title: String?, body: String?) {
        // Implementation để hiển thị notification
    }
}
```

## 🐛 Troubleshooting

### Theme không thay đổi
- Đảm bảo `activity.recreate()` được gọi
- Kiểm tra theme được áp dụng đúng

### Preferences không lưu
- Đảm bảo `sharedPref.edit().apply()` được gọi
- Kiểm tra filename: "app_settings"

### Push notification không hoạt động
- Đảm bảo FCM dependency được thêm
- Đảm bảo google-services.json được thêm
- Đảm bảo FirebaseMessagingService được implement

### Settings BottomSheet không hiển thị
- Đảm bảo Fragment Manager tồn tại
- Đảm bảo Activity là AppCompatActivity

## 📝 API Reference

### SettingsBottomSheet

```kotlin
class SettingsBottomSheet : BottomSheetDialogFragment() {
    // Public methods
    fun showSection(id: String) // Hiển thị section theo ID
}
```

### SectionChungFragment

```kotlin
class SectionChungFragment : Fragment() {
    // SharedPreferences keys
    companion object {
        const val PREF_THEME = "pref_theme"
        const val PREF_ACCENT = "pref_accent"
    }
}
```

### SectionNotificationsFragment

```kotlin
class SectionNotificationsFragment : Fragment() {
    // SharedPreferences keys
    companion object {
        const val PREF_PUSH_NOTIFICATION = "pref_push_notification"
    }
}
```

### SectionAccountFragment

```kotlin
class SectionAccountFragment : Fragment() {
    // Uses FirebaseAuth.getInstance()
}
```

## 📊 Data Models

### NavItem

```kotlin
data class NavItem(
    val id: String,      // "chung", "thongbao", "taikhoan"
    val title: String,   // "Chung", "Thông báo", "Tài khoản"
    val iconRes: Int     // drawable resource
)
```

## 🔐 Security Notes

- SharedPreferences không được encrypt (dùng EncryptedSharedPreferences nếu cần)
- Firebase Auth token được quản lý bởi Firebase SDK
- Passwords không lưu locally

## 📈 Future Enhancements

- [ ] Thêm Language selection (Tiếng Việt/English)
- [ ] Thêm Font size adjustment
- [ ] Thêm Backup preferences to cloud
- [ ] Thêm Reset to defaults button
- [ ] Thêm Preference import/export

## 🤝 Contributing

Khi thêm feature mới:
1. Tạo Fragment mới kế thừa từ Fragment
2. Thêm layout file tương ứng
3. Thêm case trong `showSection()`
4. Implement SharedPreferences persistence
5. Thêm test cases

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra TESTING_CHECKLIST.md
2. Kiểm tra logcat cho errors
3. Xem IMPLEMENTATION_GUIDE.md
4. Xem THEME_CONFIGURATION_GUIDE.md

---

**Last Updated**: December 7, 2025
**Version**: 1.0.0

