# 📚 QUICK REFERENCE - Settings Module

## 🎯 Quick Links

- **README_SETTINGS.md** - Hướng dẫn đầy đủ cách sử dụng
- **IMPLEMENTATION_GUIDE.md** - Chi tiết implementation từng chức năng
- **THEME_CONFIGURATION_GUIDE.md** - Cấu hình theme và colors
- **FAQ_SETTINGS.md** - Trả lời các câu hỏi thường gặp
- **TESTING_CHECKLIST.md** - Danh sách test case

---

## 🚀 Quick Start

### 1. Mở Settings từ Activity

```kotlin
val settingsBottomSheet = SettingsBottomSheet()
settingsBottomSheet.show(supportFragmentManager, "SettingsBottomSheet")
```

### 2. Load Theme khi App Start

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Load theme TRƯỚC setContentView
        val sharedPref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val themePos = sharedPref.getInt("pref_theme", 0)
        
        val theme = when (themePos) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO     // Light
            2 -> AppCompatDelegate.MODE_NIGHT_YES    // Dark
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // Auto
        }
        AppCompatDelegate.setDefaultNightMode(theme)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

### 3. Xử lý Logout

```kotlin
btnLogout.setOnClickListener {
    FirebaseAuth.getInstance().signOut()
    // finishAffinity() xoá toàn bộ activity stack
    requireActivity().finishAffinity()
    // App quay tự động về LoginActivity (nếu có trong manifest)
}
```

---

## 📁 File Structure

```
Fragments (3):
├── SectionChungFragment.kt
│   ├── Theme Selection (Spinner)
│   ├── Accent Color (RadioGroup)
│   └── SharedPreferences persistence
│
├── SectionNotificationsFragment.kt
│   ├── Push Notification Toggle (Switch)
│   ├── FCM integration ready
│   └── SharedPreferences persistence
│
└── SectionAccountFragment.kt
    ├── Display User Email
    ├── Logout Button
    └── Firebase Auth integration

Layouts (5):
├── fragment_settings_bottom_sheet.xml
├── item_settings_nav.xml
├── section_chung.xml
├── section_notifications.xml
└── section_account.xml
```

---

## 🔑 SharedPreferences Keys

| Key | Type | Values | Default |
|-----|------|--------|---------|
| `pref_theme` | int | 0 (Auto), 1 (Light), 2 (Dark) | 0 |
| `pref_accent` | int | 0 (Default), 1 (Blue), 2 (Red) | 0 |
| `pref_push_notification` | boolean | true/false | true |

**File Location**: `getSharedPreferences("app_settings", MODE_PRIVATE)`

---

## 🎨 Theme Implementation

### Cách 1: AppCompatDelegate (Simple)

```kotlin
// Thay đổi theme
AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

// Reload activity
requireActivity().recreate()
```

### Cách 2: Dynamic Colors (API 31+)

```kotlin
// Android 12+
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val colors = DynamicColors.getColor(context, android.R.attr.colorSecondary, Color.GRAY)
}
```

### Cách 3: Manual Style Change

```kotlin
// Trong styles.xml
<style name="AppTheme" parent="Theme.MaterialComponents">
    <item name="colorSecondary">@color/accent_default</item>
</style>

<style name="AppTheme.BlueAccent">
    <item name="colorSecondary">@color/accent_blue</item>
</style>

// Trong code
requireActivity().setTheme(R.style.AppTheme_BlueAccent)
requireActivity().recreate()
```

---

## 🔔 Push Notifications Setup

### Step 1: Add Dependency (build.gradle)

```gradle
dependencies {
    implementation 'com.google.firebase:firebase-messaging:23.2.1'
}
```

### Step 2: Create FirebaseMessagingService

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if user enabled notifications
        val sharedPref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isPushEnabled = sharedPref.getBoolean("pref_push_notification", true)
        
        if (isPushEnabled && remoteMessage.notification != null) {
            sendNotification(
                remoteMessage.notification!!.title,
                remoteMessage.notification!!.body
            )
        }
    }
    
    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, "chatbot_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
```

### Step 3: Enable in AndroidManifest.xml

```xml
<service android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### Step 4: Subscribe/Unsubscribe

```kotlin
// Subscribe
FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Subscribed successfully")
        }
    }

// Unsubscribe
FirebaseMessaging.getInstance().unsubscribeFromTopic("all_notifications")
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Unsubscribed successfully")
        }
    }
```

---

## 🧪 Common Test Cases

### Test Theme Change
```kotlin
// 1. Open Settings
// 2. Select "Tối" (Dark)
// 3. App should change to dark mode immediately
// 4. Close Settings
// 5. Open Settings again → "Tối" should be selected
// 6. Kill and restart app → Dark mode should persist
```

### Test Logout
```kotlin
// 1. Open Settings
// 2. Go to "Tài khoản" section
// 3. Click "Đăng xuất"
// 4. Verify: 
//    - Firebase user is signed out
//    - App navigates to LoginActivity
//    - All activities are cleared
```

### Test Push Notification
```kotlin
// 1. Open Settings
// 2. Go to "Thông báo" section
// 3. Toggle switch ON
// 4. Check FCM subscription in Logcat
// 5. Send test message from Firebase Console
// 6. Verify: Notification received
// 7. Toggle switch OFF
// 8. Send test message again
// 9. Verify: No notification received
```

---

## ❌ Common Mistakes

### ❌ Mistake 1: Theme không change
```kotlin
// WRONG - recreation chỉ tạo view mới, không load theme
AppCompatDelegate.setDefaultNightMode(theme)
// Missing: requireActivity().recreate()

// CORRECT
AppCompatDelegate.setDefaultNightMode(theme)
requireActivity().recreate()
```

### ❌ Mistake 2: Data mất khi back
```kotlin
// WRONG - View references không được save
private lateinit var spTheme: Spinner

override fun onViewCreated() {
    spTheme = view.findViewById(R.id.spTheme) // Null nếu recreate
}

// CORRECT - Load lại từ SharedPreferences
override fun onViewCreated() {
    spTheme = view.findViewById(R.id.spTheme)
    loadPreferences() // Restore values
}
```

### ❌ Mistake 3: Logout không work
```kotlin
// WRONG - User still logged in
FirebaseAuth.getInstance().signOut()
// Missing: requireActivity().finishAffinity()

// CORRECT
FirebaseAuth.getInstance().signOut()
requireActivity().finishAffinity()
```

### ❌ Mistake 4: Push không receive
```kotlin
// WRONG - Kiểm tra toggle nhưng không subscribe
if (isChecked) {
    // Missing: FirebaseMessaging.getInstance().subscribeToTopic()
}

// CORRECT
if (isChecked) {
    FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")
}
```

---

## 🔧 Debug Commands

### View SharedPreferences
```bash
adb shell
run-as com.example.chatbotvip
cat /data/data/com.example.chatbotvip/shared_prefs/app_settings.xml
```

### Clear SharedPreferences
```bash
adb shell
run-as com.example.chatbotvip
rm /data/data/com.example.chatbotvip/shared_prefs/app_settings.xml
```

### Check Firebase User
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
Log.d("Auth", "UID: ${currentUser?.uid}")
Log.d("Auth", "Email: ${currentUser?.email}")
Log.d("Auth", "Name: ${currentUser?.displayName}")
```

### Check FCM Token
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
    }
}
```

---

## 📊 Architecture Diagram

```
MainActivity
    ↓
[Settings Icon Click]
    ↓
SettingsBottomSheet (Parent)
    ├── Navigation RecyclerView
    │   ├── Item "Chung"
    │   ├── Item "Thông báo"
    │   └── Item "Tài khoản"
    │
    └── Fragment Container
        ├── SectionChungFragment
        │   ├── Theme Spinner
        │   ├── Accent RadioGroup
        │   └── SharedPreferences
        │
        ├── SectionNotificationsFragment
        │   ├── Push Switch
        │   ├── FCM Integration
        │   └── SharedPreferences
        │
        └── SectionAccountFragment
            ├── Email TextView
            ├── Logout Button
            └── Firebase Auth
```

---

## 💾 Persistence Flow

```
User Action → Listener → SharedPreferences → Reload on App Start
    ↓            ↓              ↓                      ↓
Theme Changed  onItemSelected  putInt()         onCreate()
Accent Changed setOnChecked    putInt()         loadPreferences()
Push Changed   setOnChecked    putBoolean()     applyTheme()
```

---

## 🎓 Learning Resources

1. **Android Documentation**
   - [AppCompatDelegate](https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate)
   - [SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)
   - [Firebase Auth](https://firebase.google.com/docs/auth)
   - [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

2. **Material Design**
   - [Material 3 Colors](https://m3.material.io/styles/color/overview)
   - [Theme Builder](https://material-foundation.github.io/material-theme-builder/)

3. **Android Jetpack**
   - [Fragment](https://developer.android.com/guide/fragments)
   - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
   - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)

---

**Quick Copy-Paste Code Snippets**: Sử dụng FAQ_SETTINGS.md
**Full Documentation**: Sử dụng README_SETTINGS.md
**Troubleshooting**: Sử dụng IMPLEMENTATION_GUIDE.md
**Testing**: Sử dụng TESTING_CHECKLIST.md

---

**Last Updated**: December 7, 2025
**Version**: 1.0.0

