# Admin Menu - Cách Truy Cập Các Chức Năng Admin

## ✅ Giờ Admin Có Thể Truy Cập:

### 1. **Admin Inbox** 📩
- Xem tất cả tin nhắn chưa trả lời từ users

### 2. **Admin Dashboard** ⚙️
- Chat trực tiếp với user
- Giải đáp câu hỏi của user

### 3. **Admin Settings** 👨‍💼
- Set role cho user (user → admin, admin → user)

### 4. **Help Chat** 💬
- Chat hỗ trợ với admin (nếu admin cũng cần help)

---

## 🚀 Cách Sử Dụng:

### Bước 1: Login với admin account
```
Email: ocheo@gmail.com
Password: (your password)
```

### Bước 2: Mở Menu (3 dấu chấm góc trên)
```
Profile → Menu
```

### Bước 3: Thấy Menu Admin
```
┌─────────────────────────────┐
│  Oc Heo                    │
│  @ocheo                     │
├─────────────────────────────┤
│ Nâng cấp gói                │
│ Cá nhân hóa                 │
│ Cài đặt                     │
├─────────────────────────────┤
│ 📩 Admin Inbox              │ ← NEW! Click để xem inbox
│ ⚙️ Admin Dashboard          │ ← NEW! Click để chat với user
│ 👨‍💼 Set Role               │ ← NEW! Click để set role
├─────────────────────────────┤
│ Trợ giúp (Help)            │
│ Đăng xuất                   │
└─────────────────────────────┘
```

---

## 📋 Chi Tiết Từng Chức Năng:

### 1️⃣ Admin Inbox

**Click "📩 Admin Inbox"**
```
┌──────────────────────────────┐
│ Tin nhắn chưa trả lời       │
├──────────────────────────────┤
│                              │
│ User 1: "Help please"       │
│ User 2: "Question..."       │
│ User 3: "Issue about..."    │
│                              │
└──────────────────────────────┘
```

**Cách dùng:**
- Click user → Mở AdminDashboardActivity
- Xem tin nhắn
- Gửi reply
- Tự động update inbox

---

### 2️⃣ Admin Dashboard

**Không cần click trực tiếp!**
- Từ Admin Inbox → Click user → Tự động mở Dashboard
- Hoặc có thể add Intent vào Menu sau

**Giao diện:**
```
┌──────────────────────────────┐
│ User: John Doe              │
├──────────────────────────────┤
│                              │
│ "Hi, can you help?"         │
│                    12:30     │
│                              │
│         "Sure! What's up?"  │
│              12:31          │
│                              │
│ [Type message...]    [Send] │
│                              │
└──────────────────────────────┘
```

---

### 3️⃣ Admin Settings (Set Role)

**Click "👨‍💼 Set Role"**
```
┌──────────────────────────────┐
│ Set User Role                │
├──────────────────────────────┤
│ Email:                       │
│ [user@gmail.com          ]   │
│                              │
│ Role:                        │
│ [User      ▼              ]  │
│ [Admin        ]              │
│                              │
│ [Get Role            ]       │
│ [Set Role            ]       │
│                              │
│ Result:                      │
│ Email: user@gmail.com        │
│ Role: user → admin           │
└──────────────────────────────┘
```

**Cách dùng:**
1. Nhập email user
2. Click "Get Role" → xem role hiện tại
3. Chọn role mới từ dropdown
4. Click "Set Role" → Update
5. Done! User sẽ có role mới

---

### 4️⃣ Help Chat (For Admin)

**Click "Trợ giúp"** → HelpActivity
- Admin có thể chat support
- Hoặc user thường có thể chat admin

---

## ✨ Workflow Hoàn Chỉnh:

### Ngày 1: Setup Admin
```
1. Signup: ocheo@gmail.com
2. Login
3. ensureUserExists() tự động set role = admin
4. Menu sẽ hiển thị admin items
```

### Ngày 2+: Use Admin Features
```
1. Login: ocheo@gmail.com
2. Mở Menu (Profile)
3. Click "📩 Admin Inbox" → Xem tin chưa trả lời
4. Click user → Chat với user (AdminDashboard)
5. Gửi reply
6. Dùng "👨‍💼 Set Role" để promot user lên admin nếu cần
```

---

## 🎯 Key Points:

✅ Menu admin chỉ hiển thị nếu `isAdmin() == true`  
✅ Admin có thể gửi message, user sẽ có `senderRole = "admin"`  
✅ Admin có thể set role cho user: user → admin  
✅ Admin có thể downgrade admin → user  
✅ Tất cả update lưu ở Firestore + Realtime DB  

---

## 🔒 Bảo Mật:

✅ Chỉ admin mới thấy admin menu  
✅ Chỉ admin mới có thể set role (Firestore rules check)  
✅ User không thể change role của mình  
✅ Default admin "ocheo@gmail.com" luôn là admin  

---

## ⚠️ Lưu Ý:

1. **Phải deploy Firestore rules** trước khi set role!
   ```bash
   firebase deploy --only firestore:rules
   ```

2. **isAdmin() check 2 điều:**
   - Email == "ocheo@gmail.com" (hardcoded)
   - Role == "admin" (từ Firestore)

3. **Menu items sẽ tự động hide** nếu user không phải admin

---

## 📝 Checklist:

- ✅ 3 admin menu items thêm vào dialog_profile.xml
- ✅ ProfileBottomSheet.kt xử lý click
- ✅ Auto show/hide dựa trên isAdmin()
- ✅ Integrate với AdminInboxActivity, AdminSettingsActivity
- ⚠️ Firestore rules phải deploy!

**Giờ bạn có thể truy cập tất cả chức năng admin!** 🎉

