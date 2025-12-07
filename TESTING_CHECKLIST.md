# 🧪 Testing Checklist - Section Chung

## ✅ Khởi tạo & Navigation

- [ ] Mở Settings BottomSheet
- [ ] Thấy RecyclerView với 3 items: "Chung", "Thông báo", "Tài khoản"
- [ ] Click item "Chung" → Hiển thị section_chung
- [ ] Click item "Thông báo" → Hiển thị section_notifications
- [ ] Click item "Tài khoản" → Hiển thị section_account

## 🎨 Section Chung - Theme Testing

### Theme Selection (Spinner)
- [ ] Spinner hiển thị đầy đủ 3 tùy chọn: "Tự động", "Sáng", "Tối"
- [ ] Chọn "Tự động" → App theo chế độ hệ thống
- [ ] Chọn "Sáng" → UI chuyển sang sáng ngay lập tức
- [ ] Chọn "Tối" → UI chuyển sang tối ngay lập tức
- [ ] Đóng Settings → Mở lại → Spinner giữ lại lựa chọn cũ
- [ ] Đóng app → Mở lại → Spinner giữ lại lựa chọn cũ

### Accent Color Selection (RadioGroup)
- [ ] Thấy 3 RadioButton: "Mặc định", "Xanh", "Đỏ"
- [ ] Chọn "Mặc định" → Colors thay đổi về mặc định
- [ ] Chọn "Xanh" → Colors thay đổi sang xanh
- [ ] Chọn "Đỏ" → Colors thay đổi sang đỏ
- [ ] Đóng Settings → Mở lại → RadioButton giữ lại lựa chọn cũ
- [ ] Đóng app → Mở lại → RadioButton giữ lại lựa chọn cũ

### Persistence Testing
- [ ] Set Theme = "Tối" + Accent = "Xanh"
- [ ] Đóng Settings
- [ ] Mở lại Settings → Kiểm tra Theme = "Tối" + Accent = "Xanh"
- [ ] Quay lại MainActivity
- [ ] Mở lại Settings → Kiểm tra giữ nguyên cài đặt
- [ ] Kill app hoàn toàn
- [ ] Mở lại app → Mở Settings → Kiểm tra giữ nguyên cài đặt

## 🔔 Section Notifications - Push Testing

### Switch - Push Notifications
- [ ] Switch hiển thị đúng vị trí
- [ ] Bật Switch (ON) → Trạng thái tương ứng được lưu
- [ ] Tắt Switch (OFF) → Trạng thái tương ứng được lưu
- [ ] Đóng Settings → Mở lại → Switch giữ lại trạng thái cũ
- [ ] Đóng app → Mở lại → Switch giữ lại trạng thái cũ

### Firebase Integration (Nếu đã tích hợp)
- [ ] Khi ON → App subscribe vào topic "all_notifications"
- [ ] Khi OFF → App unsubscribe khỏi topic "all_notifications"
- [ ] Gửi message từ Firebase Console → Nhận thông báo khi ON
- [ ] Gửi message từ Firebase Console → Không nhận khi OFF

## 👤 Section Account - Logout Testing

### Display Email
- [ ] TextView hiển thị email của currentUser
- [ ] Email hiển thị chính xác (lấy từ FirebaseAuth)
- [ ] Nếu không có user → Hiển thị "—"

### Logout Button
- [ ] Button hiển thị "Đăng xuất"
- [ ] Click button → Firebase auth sign out thành công
- [ ] Sau logout → App quay về LoginActivity
- [ ] Kiểm tra user không còn đăng nhập

### Session Management
- [ ] Đặt user đã đăng nhập
- [ ] Mở Settings → Email hiển thị chính xác
- [ ] Click Logout → Xoá tất cả activities
- [ ] Yêu cầu login lại

## 🔧 UI/UX Testing

### Layout & Design
- [ ] RecyclerView navigation hiển thị đúng
- [ ] Divider giữa nav và content
- [ ] Spinner full width và dễ đọc
- [ ] RadioGroup hiển thị tốt
- [ ] Switch centered và dễ bấm
- [ ] Button Logout có đủ padding

### Responsiveness
- [ ] Xoay device → Layout thích ứng đúng
- [ ] ScrollView hoạt động nếu content overflow
- [ ] Tất cả views responsive với size khác nhau

### Transitions
- [ ] Chuyển giữa sections mượt mà
- [ ] Không có delay hoặc lag
- [ ] Animation smooth (nếu có)

## 🐛 Error Handling

### Edge Cases
- [ ] Mở Settings khi chưa login → Xử lý exception
- [ ] Network timeout khi push notification → Hiển thị error
- [ ] Rapid clicks → App không crash

### Lifecycle
- [ ] Pause/Resume fragment → Không mất dữ liệu
- [ ] Configuration change (xoay) → Preferences được restore
- [ ] Back button → Close settings đúng cách

## 📱 Device Testing

- [ ] Test trên Android 5.0+ (API 21)
- [ ] Test trên Android 8.0 (API 26)
- [ ] Test trên Android 12.0+ (API 31)
- [ ] Test trên thiết bị thực
- [ ] Test trên emulator

## 🎯 Performance Testing

### Memory
- [ ] Không có memory leak
- [ ] SharedPreferences load nhanh
- [ ] Fragment destroy clean

### Battery
- [ ] FCM subscription không drain battery
- [ ] No infinite loops
- [ ] Efficient view binding

## 📊 Data Integrity

### SharedPreferences
- [ ] Kiểm tra data trong SharedPreferences:
  ```
  adb shell
  run-as com.example.chatbotvip
  cat /data/data/com.example.chatbotvip/shared_prefs/app_settings.xml
  ```

- [ ] "pref_theme" lưu đúng value (0, 1, 2)
- [ ] "pref_accent" lưu đúng value (0, 1, 2)
- [ ] "pref_push_notification" lưu đúng value (true, false)

### Firebase Auth
- [ ] currentUser tồn tại khi đăng nhập
- [ ] currentUser = null sau logout
- [ ] Email hiển thị chính xác

## 🎓 Integration Testing

### With MainActivity
- [ ] MainActivity nhận thay đổi theme từ Settings
- [ ] All colors update correctly
- [ ] No visual artifacts

### With LoginActivity  
- [ ] Logout quay lại LoginActivity
- [ ] Session hoàn toàn reset
- [ ] Can login again with different account

### With Firebase
- [ ] Auth integration works
- [ ] Push notification integration ready
- [ ] Data persistence works

---

## 📝 Notes

**Test ngày:** _________
**Tester:** _________
**Device:** _________
**Android Version:** _________
**Issues Found:** _________

---

## ✨ Sign Off

- [ ] Tất cả tests passed
- [ ] Code review completed
- [ ] Ready for production

**Approved by:** _________ **Date:** _________

