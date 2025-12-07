# Hướng dẫn Chức năng Settings - Section Chung

## Tổng quan
Tôi đã xây dựng một hệ thống Settings hoàn chỉnh với 3 section chính: Chung, Thông báo, và Tài khoản.

## Các file được tạo/sửa:

### 1. SectionChungFragment.kt (TẠO MỚI)
**Chức năng:** Quản lý cài đặt chung bao gồm:
- **Giao diện (Theme):**
  - Tự động: Theo hệ thống
  - Sáng: Chế độ light
  - Tối: Chế độ dark
  - Lưu trữ: SharedPreferences
  - Hiệu ứng: Recreate activity khi thay đổi

- **Màu điểm nhấn (Accent Color):**
  - Mặc định: Màu mặc định của app
  - Xanh: Màu custom
  - Đỏ: Màu custom
  - Lưu trữ: SharedPreferences

**Class liên quan:**
- AppCompatDelegate: Quản lý dark/light mode
- SharedPreferences: Lưu trữ preferences

### 2. SectionNotificationsFragment.kt (TẠO MỚI)
**Chức năng:** Quản lý cài đặt thông báo:
- **Switch bật/tắt Push Notification:**
  - Bật: Kích hoạt nhận thông báo push từ Firebase Cloud Messaging
  - Tắt: Tắt nhận thông báo
  - Lưu trữ: SharedPreferences
  - Integration: Sẵn sàng để kết nối với FCM

**Placeholder Methods:**
- enablePushNotifications(): Sẵn sàng subscribe vào FCM topic
- disablePushNotifications(): Sẵn sàng unsubscribe khỏi FCM topic

### 3. SectionAccountFragment.kt (TẠO MỚI)
**Chức năng:** Quản lý tài khoản người dùng:
- Hiển thị email đăng nhập hiện tại từ Firebase Auth
- Nút đăng xuất (Logout):
  - Đăng xuất khỏi Firebase
  - Quay về LoginActivity
  - finishAffinity() đảm bảo toàn bộ stack bị xoá

### 4. SettingsBottomSheet.kt (CẬP NHẬT)
**Thay đổi:**
- Thay đổi từ inflate layout trực tiếp sang sử dụng Fragment architecture
- Sử dụng childFragmentManager.beginTransaction() để replace fragments
- Hỗ trợ tự động cấp quyền Fragment lifecycle

### 5. item_settings_nav.xml (CẬP NHẬT)
**Thay đổi:**
- Fix lỗi InflateException bằng cách thay `?attr/colorOnSurfaceVariant` bằng `?attr/colorControlNormal`

### 6. section_chung.xml (CẬP NHẬT)
**Thay đổi:**
- Sửa width của Spinner từ `wrap_content` thành `match_parent` để hiển thị tốt hơn

## Luồng dữ liệu

### Theme Persistence:
```
User thay đổi Spinner → onItemSelected() → AppCompatDelegate.setDefaultNightMode() 
→ saveThemePreference() → SharedPreferences.putInt() → activity.recreate()
```

### Accent Color Persistence:
```
User chọn RadioButton → setOnCheckedChangeListener() → saveAccentPreference() 
→ SharedPreferences.putInt() → applyAccentColor()
```

### Push Notification:
```
User bật/tắt Switch → setOnCheckedChangeListener() → savePushNotificationPreference() 
→ SharedPreferences.putBoolean() → enable/disablePushNotifications()
```

### Logout:
```
User click Logout → FirebaseAuth.signOut() → requireActivity().finishAffinity() 
→ Quay về LoginActivity
```

## SharedPreferences Keys:
```
- "app_settings" - Tên preference file
- "pref_theme" - Lưu theme (0-2)
- "pref_accent" - Lưu accent color (0-2)
- "pref_push_notification" - Lưu trạng thái push (true/false)
```

## Để tích hợp hoàn toàn FCM:

1. Uncomment phần implementation trong SectionNotificationsFragment:
```kotlin
enablePushNotifications() {
    FirebaseMessaging.getInstance().subscribeToTopic("all_notifications")
}

disablePushNotifications() {
    FirebaseMessaging.getInstance().unsubscribeFromTopic("all_notifications")
}
```

2. Thêm FirebaseMessaging dependency nếu chưa có (trong build.gradle)

## Kiểm tra:
✅ Theme change: Thay đổi spinner → chế độ sáng/tối
✅ Accent color: Chọn radio button → lưu preference
✅ Push notifications: Bật/tắt switch → lưu preference
✅ Logout: Click nút → đăng xuất và quay về login
✅ Persistence: Đóng app và mở lại → settings được giữ lại

