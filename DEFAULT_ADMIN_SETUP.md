# Hardcoded Default Admin Setup - ocheo@gmail.com

## ✅ Cấu Hình Mặc Định

Email **"ocheo@gmail.com"** được hardcode thành admin mặc định.

### Nơi được cấu hình:
```kotlin
// File: ChatRepository.kt
companion object {
    const val DEFAULT_ADMIN_EMAIL = "ocheo@gmail.com"  // ✅ Hardcoded default admin
}
```

---

## 🔧 Cách Hoạt Động:

### 1. **Khi Login (LoginActivity)**
```kotlin
auth.signInWithEmailAndPassword(email, pass)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            lifecycleScope.launch(Dispatchers.IO) {
                repo.ensureUserExists()  // ✅ Sẽ tự động set admin
            }
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
```

### 2. **Trong ensureUserExists() - Tự động xử lý**
```kotlin
suspend fun ensureUserExists(): Boolean {
    // ...
    if (!doc.exists()) {
        // ✅ Check nếu email = DEFAULT_ADMIN_EMAIL
        val role = if (email == DEFAULT_ADMIN_EMAIL) {
            "admin"  // Set thành admin ngay lập tức
        } else {
            "user"   // User thường
        }
        // Tạo document với role đúng
    }
}
```

### 3. **Trong isAdmin() - Check để hiển thị menu**
```kotlin
suspend fun isAdmin(): Boolean {
    val currentUser = auth.currentUser
    
    // ✅ Check nếu email = DEFAULT_ADMIN_EMAIL
    if (currentUser?.email == DEFAULT_ADMIN_EMAIL) {
        return true  // Luôn là admin
    }
    
    val roleString = getUserRoleString()
    return roleString == "admin"
}
```

---

## 📋 Quy Trình Sử Dụng:

### Lần Đầu Tiên (Signup + Login):

**1. Đăng Ký:**
```
Email: ocheo@gmail.com
Password: password123
Name: Oc Heo
```

**2. Login:**
```
Email: ocheo@gmail.com
Password: password123
↓
ensureUserExists() gọi
↓
Check email == "ocheo@gmail.com"
↓
Tạo user document với role = "admin"
↓
✅ Bạn là admin!
```

### 3. Mở App:

```
Login thành công
↓
MainActivity hiện Profile menu
↓
isAdmin() check: email == "ocheo@gmail.com"
↓
Return true
↓
Menu sẽ hiển thị:
  - Trợ giúp (Help)
  - Admin - Set Role ✅ (chỉ hiển thị vì bạn là admin)
```

### 4. Set Role Cho User Khác:

```
Click "Admin - Set Role"
↓
Nhập email: user@gmail.com
↓
Chọn role: Admin
↓
Click "Set Role"
↓
repo.setUserRoleByEmail("user@gmail.com", "admin")
↓
✅ User thành admin!
```

---

## ⚙️ Các Function Liên Quan:

### ensureUserExists()
```kotlin
// Gọi khi login
// Tự động set ocheo@gmail.com = admin
// Update cả Firestore + Realtime DB
```

### isAdmin()
```kotlin
// Check nếu user là admin
// Return true nếu:
//   1. email == "ocheo@gmail.com" (hardcoded)
//   2. Hoặc role == "admin" trong Firestore
```

### setUserRoleByEmail(email, role)
```kotlin
// Set role cho user khác
// Require: current user phải là admin
// Update cả Firestore + Realtime DB
```

---

## 🔒 Bảo Mật:

1. **ocheo@gmail.com luôn là admin** - không thể downgrade
2. **Chỉ admin mới có thể set role cho người khác**
3. **Role lưu ở 2 chỗ:**
   - Firestore (source of truth)
   - Realtime DB (cho chat display)
4. **Role không bao giờ tự động downgrade**

---

## 📱 Giao Diện:

### Profile Menu:
```
┌─────────────────────────────┐
│  Oc Heo                    │
│  @ocheo                     │
├─────────────────────────────┤
│ Nâng cấp gói                │
│ Cá nhân hóa                 │
│ Cài đặt                     │
├─────────────────────────────┤
│ ✅ Admin - Set Role         │ ← Chỉ hiển thị vì admin
│ Trợ giúp (Help)            │
│ Đăng xuất                   │
└─────────────────────────────┘
```

### Admin - Set Role Screen:
```
┌─────────────────────────────┐
│  Set User Role              │
├─────────────────────────────┤
│ Email:                      │
│ [user@gmail.com          ]  │
│                             │
│ Role:                       │
│ [User        ▼           ]  │
│ [Admin          ]           │
│                             │
│ [Get Role              ]    │
│ [Set Role              ]    │
│                             │
│ Result:                     │
│ Email: user@gmail.com       │
│ Role: user                  │
└─────────────────────────────┘
```

---

## ✅ Checklist:

- ✅ DEFAULT_ADMIN_EMAIL = "ocheo@gmail.com" hardcoded
- ✅ ensureUserExists() tự động set admin
- ✅ isAdmin() check DEFAULT_ADMIN_EMAIL
- ✅ LoginActivity không cần gọi setUserAsAdmin()
- ✅ Menu Admin chỉ hiển thị cho admin
- ✅ setUserRoleByEmail() chỉ admin dùng được

**Mọi thứ đã sẵn sàng!** 🎉

Giờ bạn login = admin ngay lập tức, không cần thêm gì!

