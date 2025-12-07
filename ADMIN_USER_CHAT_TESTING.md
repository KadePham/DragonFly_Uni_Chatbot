tho# Admin-User Chat System - Testing Guide

## 📋 Setup Before Testing

### 1. Firebase Rules Configuration

Before testing, update your Firebase Security Rules to allow the chat system to work:

#### Firestore Rules
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'",
        ".write": "auth.uid == $uid",
        "conversations": {
          "$convId": {
            ".read": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'",
            ".write": "auth.uid == $uid"
          }
        }
      }
    },
    "admin_inbox": {
      "$uid": {
        ".read": "root.child('users').child(auth.uid).child('role').val() == 'admin'",
        ".write": "auth.uid == $uid"
      }
    }
  }
}
```

#### Realtime Database Rules
```json
{
  "rules": {
    "messages": {
      "$uid": {
        ".read": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'",
        ".write": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'"
      }
    }
  }
}
```

### 2. Create Test Accounts

**Regular User Account:**
- Email: `user@gmail.com`
- Password: `123456`
- Role: `user` (set automatically on first login)

**Admin Account:**
- Email: `admin@gmail.com`
- Password: `123456`
- Role: `admin`

To create admin account:
1. Open app
2. Go to Login screen
3. Click "Create Demo Admin Account" button
4. Confirms: "Tài khoản admin tạo thành công!"

---

## ✅ Test Case 1: User Sends Message to Admin

### Prerequisites:
- User account (`user@gmail.com`) logged in
- Admin account created

### Steps:
1. In app (logged in as user):
   - Open sidebar menu (click logo/menu icon)
   - Tap **"Admin Support"** menu item
   - Should open AdminChatActivity

2. In chat screen:
   - Type message: `"Test message 1"`
   - Click send button
   - Message should appear in blue bubble on right side
   - Should show timestamp (HH:MM format)
   - Should show sender name if available

3. Verify in database (Firebase Console):
   - Go to Realtime Database
   - Navigate to: `messages/{userUID}/admin_support/`
   - Should see message with structure:
     ```json
     {
       "id": "xxx",
       "chatId": "admin_support",
       "content": "Test message 1",
       "isUser": true,
       "senderName": "John",
       "senderRole": "user",
       "timestamp": {...}
     }
     ```

4. Verify admin inbox updated (Firebase Console):
   - Go to Firestore
   - Check: `admin_inbox/{userUID}`
   - Should contain:
     ```json
     {
       "lastMessage": "Test message 1",
       "unreadCount": 1,
       "lastMessageFromUser": true
     }
     ```

**✅ Expected Result:** Message appears in chat, saved in DB with correct metadata

---

## ✅ Test Case 2: Admin Receives User Message

### Prerequisites:
- User sent message (Test Case 1 completed)
- Different device/emulator for admin OR logout user first

### Steps:
1. Login as admin:
   - Email: `admin@gmail.com`
   - Password: `123456`

2. Navigate to Admin Inbox:
   - Currently needs manual Intent navigation
   - Option A: Add navigation from MainActivity (TODO)
   - Option B: Open AdminInboxActivity directly via Android Studio Device Monitor
   - Should see list of users with unread messages

3. Verify list shows:
   - User's name
   - User's email
   - Last message: "Test message 1"
   - Unread badge: "1"

4. Click on user in list:
   - Opens AdminDashboardActivity
   - Title shows: "Chat với user@gmail.com"
   - Message from user appears in gray bubble on left
   - Shows timestamp

**✅ Expected Result:** Admin sees user's message in real-time

---

## ✅ Test Case 3: Admin Sends Reply to User

### Prerequisites:
- Admin has user's chat open (Test Case 2 completed)
- User is still viewing AdminChatActivity (optional, can test on different device)

### Steps:
1. In AdminDashboardActivity (Admin chat):
   - Type message: `"Hello! We're here to help"`
   - Click send button
   - Message appears in gray bubble on left
   - Shows timestamp

2. Verify in database (Firebase Console):
   - Navigate to: `messages/{userUID}/admin_support/`
   - Should see admin's message with:
     ```json
     {
       "content": "Hello! We're here to help",
       "isUser": false,
       "senderRole": "admin",
       "senderName": "Admin" or current admin's name
     }
     ```

3. Verify conversation marked as replied (Firebase Console):
   - Check: `admin_inbox/{userUID}`
   - Should show:
     ```json
     {
       "unreadCount": 0,
       "lastMessageFromUser": false
     }
     ```

### Verify User Receives Reply:
1. If user is in AdminChatActivity on same device:
   - Message should appear automatically (real-time)
   - Shows in blue bubble? NO - should show as gray bubble (from admin)
   - Shows timestamp

2. If user on different device:
   - Close and reopen AdminChatActivity
   - Verify admin's reply appears

**✅ Expected Result:** Admin message appears in real-time on user's screen

---

## ✅ Test Case 4: Real-Time Message Synchronization

### Prerequisites:
- Two devices/emulators available
- User on Device 1, Admin on Device 2

### Steps:
1. Setup:
   - Device 1: User logged in, AdminChatActivity open
   - Device 2: Admin logged in, AdminDashboardActivity with that user open

2. User sends message (Device 1):
   - Type: `"Do you see this in real-time?"`
   - Click send
   - Should appear immediately on Device 1

3. Verify on Admin side (Device 2):
   - WITHOUT refreshing, message should appear in ~1-2 seconds
   - Shows in gray bubble on left
   - Shows timestamp

4. Admin sends message (Device 2):
   - Type: `"Yes, I see it!"`
   - Click send
   - Should appear immediately on Device 2

5. Verify on User side (Device 1):
   - WITHOUT refreshing, message should appear in ~1-2 seconds
   - Shows as gray bubble (not blue, because from admin)
   - Shows timestamp

**✅ Expected Result:** Messages sync in real-time without refresh (like Messenger)

---

## ✅ Test Case 5: Multiple Users Chatting with Admin

### Prerequisites:
- Multiple user accounts available

### Steps:
1. Login as User 1:
   - Send message: `"Hi from User 1"`

2. Switch to User 2:
   - Send message: `"Hi from User 2"`

3. Switch to User 3:
   - Send message: `"Hi from User 3"`

4. Login as Admin:
   - Open AdminInboxActivity
   - Should see 3 users in list:
     - User 3 (most recent - at top)
     - User 2 (middle)
     - User 1 (oldest - at bottom)
   - Each with their last message and unread count = 1

5. Click on User 2:
   - Should see: "Hi from User 2"
   - Other users' messages should NOT appear

6. Send reply to User 2:
   - Type: `"Hi User 2!"`
   - After sending, go back to inbox
   - User 2 should now show unreadCount = 0

7. Open User 1:
   - Should still see unreadCount = 1
   - See message: "Hi from User 1"

**✅ Expected Result:** Admin can chat with multiple users independently

---

## ✅ Test Case 6: Message Persistence

### Prerequisites:
- Have multiple messages in conversation

### Steps:
1. User in AdminChatActivity:
   - See messages 1, 2, 3

2. Close app completely
   - Wait 10 seconds
   - Reopen app

3. Login again:
   - Navigate to AdminChatActivity
   - Should still see all messages 1, 2, 3
   - Messages should be in order by timestamp

4. Check with Admin:
   - Close AdminDashboardActivity
   - Reopen it with same user
   - Should still see all messages

**✅ Expected Result:** Messages persist in database and load on app restart

---

## ✅ Test Case 7: Timestamp Display

### Prerequisites:
- Have messages in chat at different times

### Steps:
1. Check User Message:
   - Should show: `[Sender Name]` above blue bubble
   - Should show: `HH:MM` to right of bubble
   - Format should be local time (e.g., 14:30)

2. Check Admin Message:
   - Should show: `[Admin Name] (admin)` above gray bubble
   - Should show: `HH:MM` to right of bubble

3. Timestamps should be:
   - Accurate (not off by hours/timezone issues)
   - Chronologically ordered (earlier messages have earlier times)
   - Formatted as 24-hour time

**✅ Expected Result:** Timestamps display correctly and match message order

---

## ✅ Test Case 8: Admin Inbox Unread Badge

### Prerequisites:
- User has sent message(s)

### Steps:
1. Admin opens inbox:
   - User should have badge showing unread count
   - Badge should show number (1, 2, 3, etc.)

2. Admin sends reply:
   - Admin inbox still shows user, BUT
   - Unread count should change to 0 (or disappear)

3. User sends another message:
   - Admin inbox unread count should increase to 1 again

**✅ Expected Result:** Unread badges update correctly

---

## 🐛 Common Issues & Troubleshooting

### Issue: Messages not appearing in admin inbox
**Solution:**
- Check Firestore rules allow admin to read `admin_inbox` collection
- Verify `updateConversationMetadata()` is being called in `sendAdminMessage()`
- Check admin is properly marked with role "admin" in Firestore

### Issue: Real-time messages not updating
**Solution:**
- Check Realtime DB rules allow access
- Verify `ValueEventListener` is properly attached
- Check no exceptions in logcat
- Verify message is being saved correctly to Realtime DB path

### Issue: Timestamps showing wrong time
**Solution:**
- Check device time is correct
- Ensure `Timestamp.now()` is used (not manual timestamp)
- Check timezone settings

### Issue: Admin can't access AdminDashboardActivity
**Solution:**
- Verify intent is being passed with correct extras:
  - `userUID`
  - `userName`
- Check intent is being started in AdminInboxActivity

### Issue: Messages appearing in wrong order
**Solution:**
- Verify messages are sorted by timestamp
- Check Firebase timestamps are consistent
- Ensure `sortedBy` is applied in adapter

---

## 📊 Performance Considerations

**Expected Performance:**
- Message send: < 2 seconds (local UI instant + Firebase sync)
- Message receive: 1-3 seconds (realtime listener delay)
- Inbox load: 1-2 seconds (initial query + listener setup)
- Auto-scroll: Should be smooth

**If Slow:**
- Check network connection
- Verify Firebase Realtime DB quota not exceeded
- Check if too many listeners active
- Verify indexes on Firestore (if using queries)

---

## 🔍 Firebase Console Verification Checklist

After each test case, verify in Firebase Console:

### Realtime Database:
- [ ] Messages saved at `messages/{userUID}/admin_support/{msgId}`
- [ ] Message structure correct (all fields present)
- [ ] Timestamps incrementing
- [ ] Correct isUser flags (true for user, false for admin)

### Firestore:
- [ ] User document exists in `users/{uid}`
- [ ] Conversation exists at `users/{uid}/conversations/admin_support`
- [ ] Admin inbox updated at `admin_inbox/{userUID}`
- [ ] Unread count changing correctly
- [ ] lastMessageFromUser flag toggling

### Auth:
- [ ] Both user and admin accounts created
- [ ] Roles assigned correctly (user vs admin)

---

## ✅ Final Checklist

- [ ] Users can send messages to admin
- [ ] Admin receives in inbox with unread badge
- [ ] Admin can reply to user messages
- [ ] User receives replies in real-time
- [ ] Multiple users can chat independently with admin
- [ ] Timestamps display correctly
- [ ] Messages persist after app restart
- [ ] Real-time sync works (1-3 sec delay acceptable)
- [ ] Sender names display correctly
- [ ] Admin/user roles display correctly
- [ ] No crashes or major errors in logcat

---

If all test cases pass ✅, the Admin-User Chat System is ready for production!

Last Updated: December 7, 2024

