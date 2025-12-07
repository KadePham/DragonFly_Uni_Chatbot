# Cách Sử Dụng setUserAsAdmin trong LoginActivity

## 📝 Hàm mới được tạo:

```kotlin
private fun setUserAsAdmin(email: String)
```

## 🚀 Cách dùng:

### Cách 1: Gọi sau khi login thành công

```kotlin
// Trong LoginActivity.kt - sửa phần login success

auth.signInWithEmailAndPassword(email, pass)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // Ensure user document exists
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.ensureUserExists()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            
            // ✅ SET ADMIN TẠI ĐÂY
            setUserAsAdmin(email)  // Set email đang login thành admin
            
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
```

### Cách 2: Gọi với email cụ thể

```kotlin
// Ở bất kỳ đâu trong LoginActivity
setUserAsAdmin("your@email.com")
```

### Cách 3: Tạo nút "Set Admin" trong UI

```kotlin
val btnSetAdmin = findViewById<Button>(R.id.btnSetAdmin)
btnSetAdmin.setOnClickListener {
    val email = edtEmail.text.toString().trim()
    if (email.isNotEmpty()) {
        setUserAsAdmin(email)
    }
}
```

---

## ✅ Hàm làm gì?

```kotlin
private fun setUserAsAdmin(email: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            // 1. Gọi repo.setUserAsAdmin(email)
            repo.setUserAsAdmin(email)
            
            // 2. Update UI khi thành công
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@LoginActivity,
                    "✅ Set admin thành công cho: $email",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            // 3. Xử lý lỗi
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@LoginActivity,
                    "❌ Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
```

---

## 📍 Hàm trong ChatRepository:

```kotlin
suspend fun setUserAsAdmin(email: String) {
    // 1. Tìm user bằng email trong Firestore
    val query = firestore.collection("users")
        .whereEqualTo("email", email)
        .get().await()
    
    if (query.documents.isEmpty()) {
        throw Exception("User không tìm thấy")
    }

    val uid = query.documents[0].id
    
    // 2. Update role = "admin" trong Firestore
    firestore.collection("users").document(uid)
        .update(mapOf("role" to "admin"))
        .await()
    
    // 3. Update Realtime DB
    val userDoc = firestore.collection("users").document(uid).get().await()
    if (userDoc.exists()) {
        val displayName = userDoc.getString("displayName") ?: "User"
        saveUserInfoToRealtimeDB(uid, displayName, email, "admin")
    }
}
```

---

## 🎯 Ví dụ Thực Tế:

### Lần đầu tiên set admin:

**1. Đăng ký tài khoản:**
```
Email: admin@gmail.com
Password: password123
```

**2. Login lần đầu:**
```
- Gõ email & password
- Click Login
- Hàm setUserAsAdmin("admin@gmail.com") sẽ được gọi
- Toast: "✅ Set admin thành công cho: admin@gmail.com"
- Role trong Firestore sẽ đổi từ "user" → "admin"
```

**3. Mở app lần sau:**
```
- Bạn sẽ là admin
- Menu sẽ hiện "Admin - Set Role"
- Có thể set admin cho user khác
```

---

## ⚙️ Code để thêm vào LoginActivity (nếu muốn tự động set):

```kotlin
auth.signInWithEmailAndPassword(email, pass)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repo.ensureUserExists()
                    
                    // ✅ TỰ ĐỘNG SET ADMIN (option)
                    // Uncomment dòng dưới nếu muốn tất cả user login lần đầu đều thành admin
                    // repo.setUserAsAdmin(email)
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
```

---

## 🔒 Lưu ý:

1. **Email phải chính xác** - phải khớp với email trong Firestore
2. **User phải tồn tại** - phải đã đăng ký
3. **Sẽ update cả 2 chỗ:**
   - Firestore: `users/{uid}.role = "admin"`
   - Realtime DB: `users/{uid}.role = "admin"`
4. **Role sẽ giữ nguyên** - không bao giờ tự đổi thành "user" lần sau

---

## 📋 Checklist:

- ✅ Hàm `setUserAsAdmin(email)` đã được tạo trong LoginActivity
- ✅ Hàm `setUserAsAdmin(email)` đã được tạo trong ChatRepository  
- ✅ Có thể gọi từ LoginActivity bất kỳ lúc nào
- ✅ Tự động update cả Firestore + Realtime DB
- ✅ Toast thông báo kết quả

**Giờ bạn có thể set admin một cách dễ dàng!** 🎉

