# Hướng Dẫn Set Role Admin Bằng setUserRoleByEmail

## 3 Cách để Set Admin Role:

### **Cách 1: Dùng QuickAdminActivity (Đơn Giản Nhất)**

```
1. Chạy app
2. Login với email của bạn
3. Thêm Intent trong MainActivity:
   - Tạo một button "Quick Admin" trong MainActivity
   - Click button sẽ mở QuickAdminActivity
   
4. Trong QuickAdminActivity:
   - Nhập email của bạn
   - Click "Set Admin"
   - Xong! Role sẽ được set thành admin
```

**Code để thêm vào MainActivity:**
```kotlin
// Thêm vào onCreate()
val btnQuickAdmin = findViewById<Button>(R.id.btnQuickAdmin)
btnQuickAdmin.setOnClickListener {
    startActivity(Intent(this, QuickAdminActivity::class.java))
}
```

---

### **Cách 2: Dùng AdminSettingsActivity (Toàn Năng)**

```
1. Login vào app
2. Mở ProfileBottomSheet (Menu 3 chấm)
3. Click "Admin - Set Role" (chỉ hiển thị nếu bạn đã là admin)
4. Nhập email user cần set role
5. Chọn role từ dropdown
6. Click "Set Role" → Done!
```

---

### **Cách 3: Gọi Hàm Trực Tiếp Từ Code**

```kotlin
private fun setUserAsAdmin(email: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val repo = ChatRepository()
            
            // Gọi hàm setUserRoleByEmail
            @Suppress("UNRESOLVED_REFERENCE")
            repo.setUserRoleByEmail(email, "admin")
            
            // Nó sẽ:
            // 1. Tìm user bằng email trong Firestore
            // 2. Kiểm tra current user là admin (nếu không admin sẽ throw error)
            // 3. Update role thành "admin" trong Firestore
            // 4. Update role trong Realtime DB
            // 5. Log kết quả
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "✅ Set admin thành công", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "❌ Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// Gọi hàm:
setUserAsAdmin("your@email.com")
```

---

## Hàm setUserRoleByEmail - Chi Tiết Hoạt Động:

```kotlin
suspend fun setUserRoleByEmail(email: String, newRole: String)

Parameters:
  - email: Email của user cần set role (String)
  - newRole: Role mới ("user" hoặc "admin")

Returns: Unit (không return value, chỉ throw exception nếu lỗi)

Throws:
  - IllegalStateException: Nếu current user không phải admin
  - IllegalArgumentException: Nếu newRole không hợp lệ
  - Exception: Nếu user với email này không tồn tại

Side Effects:
  - Update Firestore: users/{uid}.role = newRole
  - Update Realtime DB: users/{uid}.role = newRole
  - Log chi tiết thao tác
```

---

## Ví Dụ Thực Tế:

### Ví dụ 1: Set admin cho chính mình lần đầu
```kotlin
// Bạn vừa đăng ký, chưa là admin
// Bạn có access to Firebase Console
// Thay đổi role thành "admin" ở Firebase Console

// Sau đó, bạn có thể set admin cho người khác:
repo.setUserRoleByEmail("friend@gmail.com", "admin")
```

### Ví dụ 2: Set user từ admin thành user lại
```kotlin
// Bạn là admin
// Muốn downgrade ai đó từ admin về user:
repo.setUserRoleByEmail("oldadmin@gmail.com", "user")
```

---

## Lưu Ý Quan Trọng:

1. **Chỉ admin mới được gọi hàm này**
   - Nếu không admin → throw IllegalStateException
   
2. **Email phải đúng**
   - Email phải khớp với email trong Firestore
   - Không case-sensitive (tự động xử lý)
   
3. **Role phải hợp lệ**
   - Chỉ chấp nhận "user" hoặc "admin"
   - Lowercase (tự động convert)

4. **Nó update cả 2 nơi**
   - Firestore (source of truth)
   - Realtime DB (cho chat display)

5. **Role sẽ luôn giữ nguyên**
   - Không bao giờ tự đổi thành "user" nữa
   - Vì setUserRoleByEmail kiểm tra role hiện tại từ Firestore

---

## Troubleshooting:

**Lỗi: "Chỉ admin mới có thể cập nhật role"**
→ Current user không phải admin. Bạn cần set admin ở Firebase Console trước.

**Lỗi: "User with email X not found"**
→ Email nhập sai hoặc user chưa được tạo.

**Lỗi: "Role không hợp lệ"**
→ Chỉ dùng "user" hoặc "admin", không dùng "User" hay "ADMIN".

**Role vẫn là "user" sau khi set**
→ Refresh app, đợi vài giây, hoặc check Firestore trực tiếp.


