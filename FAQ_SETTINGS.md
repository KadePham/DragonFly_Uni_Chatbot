# ❓ FAQ - Settings Module

## Câu hỏi Thường Gặp

### Theme & Appearance

**Q: Làm sao để theme change tức thời?**
A: Gọi `activity.recreate()` sau khi set theme
```kotlin
AppCompatDelegate.setDefaultNightMode(theme)
activity.recreate()
```

**Q: Theme có được lưu lại sau khi đóng app không?**
A: Có, chúng ta lưu trong SharedPreferences và load lại trong MainActivity.onCreate()

**Q: Làm sao để thêm thêm theme mới (ví dụ Light Blue)?**
A: 
1. Thêm tùy chọn vào `section_chung.xml` RadioButton mới
2. Thêm color definitions trong `colors.xml`
3. Thêm case mới trong `saveAccentPreference()`
4. Thêm style theme mới trong `styles.xml` nếu dùng Material3

**Q: Accent color không thay đổi, sao vậy?**
A: Cần implement `applyAccentColor()` và gọi `activity.recreate()` hoặc dùng Dynamic Colors (API 31+)

**Q: Làm sao để đặt theme mặc định khi first launch?**
A: SharedPreferences default value là 0 (Tự động), tự động được load nếu key không tồn tại

---

### Notifications

**Q: Push notification không hoạt động, làm sao?**
A: Kiểm tra:
1. google-services.json được thêm đúng
2. Firebase dependency được thêm
3. FirebaseMessagingService được implement
4. user permission được enable

**Q: Làm sao để subscribe/unsubscribe FCM topic?**
A:
```kotlin
// Subscribe
FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")

// Unsubscribe
FirebaseMessaging.getInstance().unsubscribeFromTopic("all_notifications")
```

**Q: Push notification có được persist không khi kill app?**
A: Subscription lưu trong Firebase, nhưng local preference lưu trong SharedPreferences

**Q: Làm sao để test push notification?**
A: Dùng Firebase Console → Cloud Messaging → Send test message đến topic "all_notifications"

**Q: Có cách nào để group notifications không?**
A: Có, dùng `setGroup()` khi build notification
```kotlin
.setGroup("chatbot_notifications")
.setGroupSummary(true)
```

---

### Account & Authentication

**Q: Làm sao để hiển thị user info khác (phone, profile)?**
A:
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
tvPhone.text = currentUser?.phoneNumber ?: "—"
tvName.text = currentUser?.displayName ?: "—"
```

**Q: Logout có xoá local data không?**
A: Không, chỉ xoá Firebase session. Dùng `context.deleteDatabase()` nếu cần xoá local data

**Q: Làm sao để prevent logout khi có pending requests?**
A:
```kotlin
btnLogout.setOnClickListener {
    if (isPendingRequests) {
        showDialog("Vui lòng chờ tất cả request hoàn thành")
    } else {
        FirebaseAuth.getInstance().signOut()
        requireActivity().finishAffinity()
    }
}
```

**Q: Có thể logout khỏi tất cả device không?**
A: Không trực tiếp, nhưng có thể revoke token từ server:
```kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        // Token refreshed, có thể dùng để revoke trên server
        val token = task.result?.token
    }
}
```

---

### SharedPreferences & Storage

**Q: Data được lưu ở đâu?**
A: `/data/data/com.example.chatbotvip/shared_prefs/app_settings.xml`

**Q: Làm sao để xem data trong SharedPreferences?**
A:
```bash
adb shell
run-as com.example.chatbotvip
cat /data/data/com.example.chatbotvip/shared_prefs/app_settings.xml
```

**Q: Có cách nào để encrypt SharedPreferences không?**
A: Có, dùng EncryptedSharedPreferences:
```kotlin
val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "app_settings_encrypted",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**Q: Làm sao để backup preferences?**
A: Implement custom backup agent hoặc save/restore manually:
```kotlin
fun backupPreferences() {
    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
    val data = prefs.all
    val json = Gson().toJson(data)
    // Gửi lên server
}

fun restorePreferences(json: String) {
    val data = Gson().fromJson(json, Map::class.java)
    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
    prefs.edit().clear().apply()
    for ((key, value) in data) {
        when (value) {
            is Int -> prefs.edit().putInt(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            is String -> prefs.edit().putString(key, value).apply()
        }
    }
}
```

**Q: Làm sao để reset preferences về default?**
A:
```kotlin
fun resetPreferences() {
    val sharedPref = context.getSharedPreferences("app_settings", MODE_PRIVATE)
    sharedPref.edit().clear().apply()
    // Reload default values
    loadPreferences()
}
```

---

### Fragment Lifecycle

**Q: Data bị mất khi xoay device, sao vậy?**
A: Fragment tự động reload preferences từ SharedPreferences khi recreate

**Q: Làm sao để prevent fragment recreate?**
A:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true // Deprecated, dùng ViewModel thay vì
}
```

**Q: Nên dùng ViewModel hay SharedPreferences?**
A: 
- **ViewModel**: Cho temporary data, dùng trong session
- **SharedPreferences**: Cho persistent data, dùng lâu dài

**Q: Làm sao để handle back button?**
A:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismiss() // Đóng BottomSheet
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, callback)
}
```

---

### UI/UX Issues

**Q: Spinner entries không hiển thị, sao vậy?**
A: Kiểm tra:
1. `@array/pref_theme_values` được định nghĩa trong strings.xml
2. String array có items

**Q: RadioButton không được highlight khi selected?**
A: Thêm custom drawable:
```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_checked="true" android:drawable="@drawable/ic_radio_checked"/>
    <item android:drawable="@drawable/ic_radio_unchecked"/>
</selector>
```

**Q: Switch text không align đúng?**
A: Thêm layout:
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="horizontal">
    <TextView android:text="Push Notification" android:layout_weight="1"/>
    <Switch android:id="@+id/switchPush" android:layout_width="wrap_content"/>
</LinearLayout>
```

**Q: ScrollView không scroll khi content overflow?**
A: Đảm bảo parent layout có height = match_parent:
```xml
<ScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <!-- Content -->
</ScrollView>
```

---

### Performance & Optimization

**Q: SharedPreferences load chậm, sao vậy?**
A: Chỉ load preferences cần thiết:
```kotlin
// Bad
val allPrefs = sharedPref.all // Load tất cả

// Good
val theme = sharedPref.getInt("pref_theme", 0)
val accent = sharedPref.getInt("pref_accent", 0)
```

**Q: Có memory leak không?**
A: Không, nhưng đảm bảo remove listener:
```kotlin
override fun onDestroyView() {
    spTheme.onItemSelectedListener = null
    radioAccent.setOnCheckedChangeListener(null)
    switchPush.setOnCheckedChangeListener(null)
    btnLogout.setOnClickListener(null)
    super.onDestroyView()
}
```

**Q: Fragment destroy khi nào?**
A: Khi BottomSheet dismiss hoặc navigate đi

---

### Testing

**Q: Làm sao để test preferences?**
A:
```kotlin
@Test
fun testThemePreference() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    prefs.edit().putInt("pref_theme", 2).apply()
    assertEquals(2, prefs.getInt("pref_theme", 0))
}
```

**Q: Làm sao để test logout?**
A:
```kotlin
@Test
fun testLogout() {
    val auth = FirebaseAuth.getInstance()
    auth.signOut()
    assertNull(auth.currentUser)
}
```

---

### Debugging

**Q: Làm sao để debug SharedPreferences?**
A:
```kotlin
fun logPreferences() {
    val sharedPref = context.getSharedPreferences("app_settings", MODE_PRIVATE)
    for ((key, value) in sharedPref.all) {
        Log.d("PREFS", "$key = $value")
    }
}
```

**Q: Làm sao để debug fragment lifecycle?**
A:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("Fragment", "onCreate")
}

override fun onCreateView(...): View? {
    Log.d("Fragment", "onCreateView")
    return ...
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Log.d("Fragment", "onViewCreated")
}

override fun onDestroyView() {
    Log.d("Fragment", "onDestroyView")
    super.onDestroyView()
}
```

**Q: Làm sao để debug theme change?**
A:
```kotlin
fun setupThemeSpinner() {
    spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            Log.d("Theme", "Selected position: $position")
            val theme = when (position) {
                0 -> {
                    Log.d("Theme", "MODE_NIGHT_FOLLOW_SYSTEM")
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                1 -> {
                    Log.d("Theme", "MODE_NIGHT_NO")
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                2 -> {
                    Log.d("Theme", "MODE_NIGHT_YES")
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(theme)
            saveThemePreference(position)
            Log.d("Theme", "Theme set, recreating activity")
            requireActivity().recreate()
        }
        
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}
```

---

## 💡 Tips & Tricks

1. **Dùng LiveData để observe preferences change**
   ```kotlin
   val themeLiveData = MutableLiveData<Int>()
   themeLiveData.observe(viewLifecycleOwner) { theme ->
       applyTheme(theme)
   }
   ```

2. **Dùng DataStore thay vì SharedPreferences (modern approach)**
   ```kotlin
   val dataStore: DataStore<Preferences> = context.createDataStore(name = "settings")
   val THEME_KEY = intPreferencesKey("pref_theme")
   
   val themeFlow: Flow<Int> = dataStore.data.map { preferences ->
       preferences[THEME_KEY] ?: 0
   }
   ```

3. **Dùng Compose cho modern UI**
   ```kotlin
   @Composable
   fun ThemeSelector() {
       var selectedTheme by remember { mutableStateOf(0) }
       Spinner(
           selected = selectedTheme,
           onSelectionChanged = { selectedTheme = it }
       )
   }
   ```

4. **Dùng WorkManager để sync preferences**
   ```kotlin
   val syncPrefsWork = OneTimeWorkRequestBuilder<SyncPreferencesWorker>().build()
   WorkManager.getInstance(context).enqueueUniqueWork(
       "sync_prefs",
       ExistingWorkPolicy.KEEP,
       syncPrefsWork
   )
   ```

---

**Last Updated**: December 7, 2025
**Version**: 1.0.0

