# Deploy Firestore Security Rules

## ⚠️ QUAN TRỌNG: Rules chưa được deploy!

Bạn vừa fix rules nhưng chúng chưa lên Firebase. Cần deploy ngay để app hoạt động.

---

## 🚀 Cách 1: Dùng Firebase CLI (Nhanh nhất)

### 1. Install Firebase CLI (nếu chưa có)
```bash
npm install -g firebase-tools
```

### 2. Login vào Firebase
```bash
firebase login
```

### 3. Deploy rules
```bash
cd f:\HOCKITLON\ChatBot
firebase deploy --only firestore:rules
```

### Output thành công:
```
✔  Deploy complete!

Project Console: https://console.firebase.google.com/project/...
```

---

## 🚀 Cách 2: Dùng Firebase Console (Dễ hơn)

### 1. Mở Firebase Console
```
https://console.firebase.google.com
```

### 2. Chọn project của bạn

### 3. Firestore Database → Rules

### 4. Copy toàn bộ nội dung từ file:
```
firestore.rules
```

### 5. Paste vào Firebase Console

### 6. Click "Publish"

### Output:
```
✓ Published rules for database (default)
```

---

## ✅ Verify Rules Được Deploy

### 1. Mở Firebase Console

### 2. Firestore Database → Rules

### 3. Kiểm tra các dòng:
```
function isAdmin() {
  return request.auth != null &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
}

function isDefaultAdmin() {
  return request.auth != null &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.email == 'ocheo@gmail.com';
}
```

✅ Nếu thấy thế là đã deploy đúng!

---

## 🔑 Key Changes (Rules mới):

1. **Thêm helper functions:**
   ```
   isAdmin() - Check role == 'admin'
   isDefaultAdmin() - Check email == 'ocheo@gmail.com'
   ```

2. **Admin có thể update role:**
   ```
   allow update: if isAdmin() || isDefaultAdmin()
   ```

3. **User thường KHÔNG thể change role:**
   ```
   !('role' in request.data.diff(resource.data).affectedKeys())
   ```

---

## 🧪 Test sau khi deploy:

### 1. Open app
### 2. Login: ocheo@gmail.com
### 3. Menu → Admin - Set Role
### 4. Enter email: user@gmail.com
### 5. Set role: Admin
### 6. Click "Set Role"

**Nếu thành công:** ✅ Không có PERMISSION_DENIED error

---

## 🔧 Nếu vẫn gặp lỗi PERMISSION_DENIED:

1. **Check deployed rules:**
   - Firebase Console → Firestore → Rules
   - Verify rules được update

2. **Clear app cache:**
   - Settings → Apps → Clear Cache

3. **Restart app**

4. **Check Firestore:**
   - Xem user document có role = "admin" không

5. **Check Firebase Console:**
   - Firestore → Data → users → Check role field

---

## 📋 Checklist:

- ⚠️ Rules file chỉnh sửa: firestore.rules
- ❌ Rules chưa deploy lên Firebase
- 🔴 Cần bạn deploy ngay bây giờ!

**Hãy deploy rules theo Cách 1 hoặc Cách 2 ở trên!**

Sau khi deploy, quay lại app và thử set role lại.

✅ Error sẽ biến mất!

