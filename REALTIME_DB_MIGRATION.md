# Firebase Realtime Database Migration - Messages

## Tóm tắt thay đổi
- ✅ **Messages**: Chuyển từ Firestore → Firebase Realtime Database (dễ đọc, real-time)
- ✅ **User Accounts**: Giữ lại Firestore (email, displayName, role, active status)
- ✅ **Conversation Metadata**: Giữ lại Firestore (title, lastUpdated)

---

## Cấu trúc dữ liệu

### Firestore (User Accounts & Metadata)
```
users/
├── {uid}/
│   ├── displayName: "Anh Tu"
│   ├── email: "tuvip123@gmail.com"
│   ├── role: "admin" | "user"
│   ├── active: true
│   ├── createdAt: 1765115304548
│   └── conversations/
│       └── {convId}/
│           ├── title: "Hỗ trợ kỹ thuật"
│           ├── lastUpdated: 1765115304548
│           └── (metadata only - messages elsewhere)
```

### Firebase Realtime Database (Messages)
```
messages/
├── {userId}/
│   └── {convId}/
│       └── {msgId}/
│           ├── id: "msg-123"
│           ├── chatId: "admin_support"
│           ├── content: "Xin chào"
│           ├── isUser: true
│           ├── senderUid: "uid-123"
│           ├── senderName: "Anh Tu"
│           ├── senderRole: "user"
│           ├── timestamp:
│           │   ├── seconds: 1765115304
│           │   └── nanoseconds: 548000000
│           ├── edited: false
│           └── editedAt: null
```

---

## Các file đã sửa

### 1. ChatRepository.kt
- ✅ Thêm `private val database = FirebaseDatabase.getInstance().reference`
- ✅ Sửa `getMessagesRealtime()` - đọc từ Realtime DB
- ✅ Sửa `sendMessage()` - ghi vào Realtime DB
- ✅ Sửa `deleteConversation()` - xóa messages từ Realtime DB
- ✅ Sửa `updateMessage()` - cập nhật messages trong Realtime DB

### 2. AdminDashboardActivity.kt
- ✅ Thêm Firebase Database imports
- ✅ Sửa `loadAdminChatMessages()` - đọc từ Realtime DB
- ✅ Sửa `sendAdminReply()` - ghi vào Realtime DB

### 3. firestore.rules
- ✅ Loại bỏ rules cho messages (vì chúng ở Realtime DB)
- ✅ Giữ lại rules cho users và conversations

### 4. database.rules.json (NEW)
- ✅ Rules cho Realtime Database
- ✅ User chỉ đọc/ghi tin nhắn của chính họ
- ✅ Admin có thể đọc tất cả tin nhắn

---

## Cách triển khai

### Bước 1: Deploy Firestore Rules
1. Firebase Console → Firestore Database → Rules
2. Paste nội dung từ `firestore.rules`
3. Click "Publish"

### Bước 2: Deploy Realtime Database Rules
1. Firebase Console → Realtime Database → Rules
2. Paste nội dung từ `database.rules.json`
3. Click "Publish"

### Bước 3: Rebuild & Test
```bash
# Clean build
./gradlew clean
./gradlew build
```

---

## Migration Data (nếu có dữ liệu cũ)

Để migrate dữ liệu từ Firestore → Realtime DB:

```kotlin
// Hàm migration (chỉ chạy 1 lần)
suspend fun migrateMessagesFromFirestore() {
    val firestore = FirebaseFirestore.getInstance()
    val database = FirebaseDatabase.getInstance().reference
    
    val usersRef = firestore.collection("users").get().await()
    for (userDoc in usersRef.documents) {
        val uid = userDoc.id
        val convRef = userDoc.reference
            .collection("conversations").get().await()
        
        for (convDoc in convRef.documents) {
            val convId = convDoc.id
            val msgsRef = convDoc.reference
                .collection("messages").get().await()
            
            for (msgDoc in msgsRef.documents) {
                val msgId = msgDoc.id
                val data = msgDoc.data ?: continue
                
                // Ghi vào Realtime DB
                database.child("messages").child(uid).child(convId)
                    .child(msgId).setValue(data).await()
            }
        }
    }
}
```

---

## Lợi ích của Realtime Database cho Messages

✅ **Real-time updates** - Tin nhắn cập nhật ngay lập tức mà không cần polling
✅ **Better structure** - Dữ liệu tin nhắn có cấu trúc rõ ràng hơn
✅ **Easier to read** - Có thể duyệt dữ liệu dễ dàng trong Firebase Console
✅ **Better sync** - Offline support và auto-sync khi online
✅ **Cheaper** - Realtime DB rẻ hơn Firestore cho read operations
✅ **Separate concerns** - Firestore cho accounts, Realtime DB cho messages

---

## Troubleshooting

### Messages không hiển thị
- Kiểm tra rules có allow read không
- Kiểm tra path: `messages/{uid}/{convId}/{msgId}`
- Check Android logs: `adb logcat | grep ChatRepository`

### Admin không thấy tin nhắn của user
- Kiểm tra user của admin có `role = "admin"` trong Firestore không
- Kiểm tra database rules: `.read: "auth.uid === $uid || ... role === 'admin'"`

### Timestamp không đúng
- Realtime DB lưu timestamp dạng: `{ seconds: 1234567890, nanoseconds: 548000000 }`
- Message.fromMap() tự động convert về Firestore Timestamp format

---

## Testing Checklist

- [ ] User có thể gửi tin nhắn
- [ ] User thấy tin nhắn của chính mình
- [ ] Admin thấy tin nhắn từ tất cả user
- [ ] Tin nhắn được sắp xếp theo thời gian
- [ ] Admin có thể reply tin nhắn
- [ ] Conversation metadata được cập nhật trong Firestore
- [ ] Xóa conversation xóa messages từ Realtime DB
- [ ] Edit message cập nhật trong Realtime DB


