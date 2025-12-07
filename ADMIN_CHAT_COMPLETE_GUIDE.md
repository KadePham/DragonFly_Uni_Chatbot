# Admin Chat Support System - Complete Guide

## 📋 Overview
Hệ thống này cho phép:
- **Users**: Nhắn tin trực tiếp với Admin Support qua nút "Trợ giúp" trong profile menu
- **Admins**: Xem và reply messages từ tất cả users qua AdminDashboardActivity

## 🏗️ Architecture

### Components Added:

#### 1. **AdminChatActivity.kt** - User Chat Interface
- Activity để người dùng nhắn tin với admin
- Quản lý sending/receiving messages realtime
- Sử dụng RecyclerView để hiển thị tin nhắn
- Auto-scroll to latest message

#### 2. **AdminDashboardActivity.kt** - Admin Interface (NEW)
- Activity dành cho admin xem messages từ các users
- Cho phép admin reply messages
- Tương tự AdminChatActivity nhưng:
  - Sets `isUser = false` khi gửi reply
  - Nhận `userUID` từ intent para để biết user nào

#### 3. **ChatRepository.kt - New Methods**
```kotlin
// Lấy hoặc tạo conversation với admin
suspend fun getOrCreateAdminChat(): String

// Lắng nghe tin nhắn realtime từ admin chat
fun getAdminMessagesRealtime(): Flow<List<Message>>

// Gửi tin nhắn tới admin
suspend fun sendAdminMessage(message: Message)
```

#### 4. **ProfileBottomSheet.kt** - Updated
- Thêm listener cho `itemHelp`
- Click itemHelp → mở AdminChatActivity

#### 5. **Layouts**
- `activity_admin_chat.xml` - User chat interface
- `activity_admin_dashboard.xml` - Admin interface

## 🗄️ Firestore Structure

```
users/{uid}/conversations/admin_support/
├── id: "admin_support"
├── title: "Chat với Admin Support"
├── ownerId: {uid}
├── lastUpdated: Timestamp
└── messages/{messageId}/
    ├── id: String
    ├── chatId: "admin_support"
    ├── isUser: Boolean (true = user message, false = admin reply)
    ├── content: String
    ├── timestamp: Timestamp
    ├── edited: Boolean
    └── editedAt: Timestamp (optional)
```

## 🔧 Setup Instructions

### Step 1: Create Admin Account (Firebase Console)
1. Go to: Firebase Console > Authentication > Users
2. Click "Add user"
3. Email: `admin@chatbot.com` (hoặc tùy ý)
4. Password: Tạo password mạnh
5. Create user

### Step 2: Update ADMIN_UID in ChatRepository
1. Copy Admin UID từ Firebase Console
2. Mở `ChatRepository.kt`
3. Tìm companion object:
```kotlin
companion object {
    const val ADMIN_UID = "YOUR_ADMIN_UID_HERE"  // ← Replace this
    const val ADMIN_CHAT_ID = "admin_support"
}
```

### Step 3: Configure Firestore Rules
1. Go to: Firebase Console > Firestore Database > Rules
2. Copy từ `firestore.rules` file
3. Paste vào Firestore Rules editor
4. Publish

### Step 4: Test

**As a Regular User:**
1. Run app và login với user account
2. Click Profile icon (top left)
3. Click "Trợ giúp" (Help)
4. Send a test message
5. Verify message appears in Firestore

**As Admin (Test Only):**
1. Duplicate main activity
2. Pass userUID as intent extra
3. Open AdminDashboardActivity
4. Send reply message
5. Verify reply appears realtime for user

## 💬 Complete Message Flow

### User Sends Message:
```
User App (AdminChatActivity)
    ↓ Click Send
    ↓ sendAdminMessage(message)
    ↓
Firestore: users/{uid}/conversations/admin_support/messages/{msgId}
    ↓
AdminChatActivity (realtime listener)
    ↓
UI Updates: Message appears in RecyclerView
```

### Admin Sends Reply:
```
Admin (AdminDashboardActivity)
    ↓ Click Send
    ↓ sendAdminReply(text) → sets isUser = false
    ↓
Firestore: users/{uid}/conversations/admin_support/messages/{msgId}
    ↓
User's AdminChatActivity (realtime listener)
    ↓
UI Updates: Reply appears with different style (isUser=false)
```

## 🎯 AdminDashboardActivity Details

### How to Open from Admin Interface:
```kotlin
// Example: dari list of users
val intent = Intent(context, AdminDashboardActivity::class.java)
intent.putExtra("userUID", selectedUserUID)
startActivity(intent)
```

### Features:
✅ Realtime message display
✅ Send replies as admin
✅ Auto-scroll to latest
✅ Beautiful UI matching ChatGPT style
✅ Toast notifications for errors

### Current Limitations:
⚠️ Anyone can open (no auth check)
⚠️ No user list view
⚠️ No notification when new message arrives

## 🔐 Security Considerations

### Current State:
- Firestore Rules: Simple authentication (user can only access own docs)
- AdminDashboardActivity: Open to anyone (needs auth)

### Recommended Improvements:
1. **Admin Authentication**
   - Custom claims in Firebase Auth
   - Check `request.auth.token.admin == true`
   - Implement admin login screen

2. **Firestore Rules** (Enhanced)
```
match /users/{uid}/conversations/{convId}/messages/{msgId} {
  // Users can only read their own
  allow read: if request.auth.uid == uid;
  allow write: if request.auth.uid == uid;
  
  // Admins can read all (if authenticated as admin)
  allow read: if isAdmin();
}

function isAdmin() {
  return request.auth.token.admin == true;
}
```

## 📦 Dependencies (Already Included)
- Firebase Auth
- Firebase Firestore
- Firebase Storage
- Kotlin Coroutines
- androidx.lifecycle
- androidx.recyclerview

## ✅ Implementation Checklist

- [x] AdminChatActivity created
- [x] AdminDashboardActivity created
- [x] activity_admin_chat.xml layout
- [x] activity_admin_dashboard.xml layout
- [x] ChatRepository methods added
- [x] ProfileBottomSheet listener added
- [x] AndroidManifest.xml updated
- [x] firestore.rules provided
- [ ] Admin account created in Firebase
- [ ] ADMIN_UID updated in ChatRepository
- [ ] Firestore Rules deployed
- [ ] User testing completed

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Messages not loading | Check Firestore rules, verify auth |
| Can't send message | Check internet, verify Firestore write permission |
| Admin can't reply | Verify userUID is passed correctly |
| Realtime not updating | Check Firestore listener registration |
| Build errors | Make sure all imports are correct |

## 📱 Future Enhancements

1. **Admin Portal**
   - Web dashboard for admins
   - List of all conversations
   - Notification system

2. **User Experience**
   - Push notifications for new replies
   - Typing indicator
   - Read receipts
   - Message reactions
   - File sharing

3. **Advanced Features**
   - Ticket/support system
   - Auto-responses
   - Message templates
   - Analytics dashboard
   - Chat export/archive

## 🔗 Related Files
- `AdminChatActivity.kt` - User side chat
- `AdminDashboardActivity.kt` - Admin side chat
- `activity_admin_chat.xml` - User chat layout
- `activity_admin_dashboard.xml` - Admin chat layout
- `ChatRepository.kt` - Firestore operations
- `ProfileBottomSheet.kt` - Help button trigger
- `firestore.rules` - Security rules
- `AndroidManifest.xml` - Activity registration

## 📞 Support
Untuk pertanyaan atau issues, lihat IMPLEMENTATION_GUIDE.md

