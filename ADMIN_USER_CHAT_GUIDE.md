# Admin-User Chat System - Implementation Guide

## 📋 Overview

This document describes the complete implementation of a **Realtime Admin-User Chat System** similar to Messenger or WhatsApp. The system allows users to chat with admin support and admin to respond to multiple users simultaneously.

---

## 🏗️ Architecture

### Data Storage

The system uses a **hybrid approach**:

- **Firestore**: User profiles and conversation metadata
- **Realtime Database**: Message content (for real-time syncing)

### Message Storage Structure

```
Firebase Realtime Database:
├── messages/
│   ├── {userUID}/
│   │   └── admin_support/
│   │       ├── {msgId1}
│   │       │   ├── id
│   │       │   ├── chatId: "admin_support"
│   │       │   ├── content: "User message text"
│   │       │   ├── timestamp: 1234567890
│   │       │   ├── isUser: true/false
│   │       │   ├── senderUid: "uid123"
│   │       │   ├── senderName: "John Doe"
│   │       │   └── senderRole: "user" or "admin"
│   │       └── {msgId2}
│   │           └── ...
│   └── {otherUserUID}/
│       └── admin_support/
│           └── ...

Firestore:
├── users/
│   ├── {userUID}/
│   │   ├── uid
│   │   ├── email
│   │   ├── displayName
│   │   ├── role: "user" or "admin"
│   │   └── conversations/
│   │       └── admin_support/
│   │           ├── id: "admin_support"
│   │           ├── title: "Chat với Admin Support"
│   │           └── lastUpdated: timestamp
│   └── {otherUserUID}/
│       └── ...

└── admin_inbox/
    ├── {userUID}/
    │   ├── userId
    │   ├── userName
    │   ├── userEmail
    │   ├── lastMessage
    │   ├── lastMessageTime
    │   ├── unreadCount: 0-99
    │   ├── lastMessageFromUser: true/false
    │   └── isResolved: true/false
    └── {otherUserUID}/
        └── ...
```

---

## 🔄 Chat Flow

### User Side

1. **User opens "Admin Support"** from sidebar menu
   - `AdminChatActivity` loads
   - Initializes conversation in Firestore if not exists
   - Subscribes to realtime messages

2. **User sends message**
   - Message saved to `Realtime DB: messages/{userUID}/admin_support/{msgId}`
   - Updates conversation metadata
   - Admin inbox gets notified with new message

3. **User receives admin reply**
   - Message listener streams new messages
   - UI updates in realtime
   - Shows sender name and timestamp

### Admin Side

1. **Admin opens Admin Inbox** (`AdminInboxActivity`)
   - Shows list of all users with unread messages
   - Sorted by `lastMessageTime` (most recent first)
   - Shows unread badge and last message preview

2. **Admin clicks on user conversation**
   - Opens `AdminDashboardActivity` with that user's messages
   - Loads all messages from `Realtime DB: messages/{userUID}/admin_support/`
   - Subscribes to realtime updates

3. **Admin sends reply**
   - Message saved to same location in Realtime DB
   - Marks conversation as "replied" (unreadCount = 0)
   - User receives notification

---

## 📁 Key Files

### Models
- **Message.kt**: Message data class with Firestore serialization
  - Stores: id, chatId, content, timestamp, senderUid, senderName, senderRole, isUser
  
- **Chat.kt**: Conversation metadata
  - Stores: id, title, ownerId, lastUpdated

- **ConversationMetadata.kt**: Admin inbox metadata
  - Stores: userId, userName, lastMessage, unreadCount, isResolved

### Views/Activities
- **AdminChatActivity.kt**: User's chat with admin
  - Loads messages from Realtime DB
  - Sends messages to admin
  - Updates admin inbox metadata

- **AdminDashboardActivity.kt**: Admin's chat with specific user
  - Admin-only activity (no role check implemented - TODO)
  - Loads user's messages from Realtime DB
  - Sends replies to user
  - Marks conversation as replied

- **AdminInboxActivity.kt**: Admin's inbox
  - Shows list of all users with unread messages
  - Click to open AdminDashboardActivity with that user

- **MainActivity.kt**: User's main chat interface
  - Added "Admin Support" button in sidebar
  - Navigates to AdminChatActivity

### Adapters
- **ChatAdapter.kt**: Displays messages
  - TYPE_USER: Blue bubbles on right (user messages)
  - TYPE_BOT: Gray bubbles on left (admin messages)
  - Shows sender name and timestamp

- **AdminInboxAdapter.kt**: Displays inbox items
  - User avatar (placeholder)
  - User name and email
  - Last message preview
  - Unread badge

### Repository
- **ChatRepository.kt**: Central data access
  - `getOrCreateAdminChat()`: Creates conversation metadata
  - `sendAdminMessage()`: User sends message to admin
  - `sendAdminReply()`: Admin sends message to user
  - `getAdminMessagesRealtime()`: User's incoming messages
  - `getAdminMessagesForUserRealtime()`: Admin's incoming messages
  - `updateConversationMetadata()`: Updates admin inbox
  - `getAdminInboxRealtime()`: Admin's inbox list
  - `markConversationAsReplied()`: Mark as replied

### Layouts
- **activity_admin_chat.xml**: User's chat screen
- **activity_admin_dashboard.xml**: Admin's chat screen
- **activity_admin_inbox.xml**: Admin's inbox screen
- **item_user.xml**: User message bubble with timestamp
- **item_bot.xml**: Admin message bubble with timestamp
- **item_admin_inbox.xml**: Inbox list item

---

## 🚀 Usage Instructions

### For Users

1. **Open app and login**
   - Go to MainActivity (main chat screen)

2. **Click "Admin Support" in sidebar**
   - Opens AdminChatActivity
   - Shows previous messages with admin (if any)

3. **Send message**
   - Type in EditText
   - Tap send button
   - Message appears immediately on screen (blue bubble)

4. **Receive admin reply**
   - Admin's reply appears as gray bubble
   - Shows admin name and timestamp
   - Real-time update (no need to refresh)

### For Admin

1. **Create admin account** (if not exists)
   - Click "Create Demo Admin Account" on login screen
   - Email: `admin@gmail.com`
   - Password: `123456`

2. **Login with admin account**
   - Same login screen

3. **Open Admin Inbox**
   - Currently accessible via Intent (need to add navigation)
   - Shows all users with unread messages
   - Click on user to open chat

4. **Chat with user**
   - See all messages from user
   - Type reply and send
   - Message appears as gray bubble (isUser=false)
   - Conversation marked as replied (unreadCount=0)

---

## 🔧 How It Works - Technical Details

### Sending User Message

```kotlin
// User sends message
val message = Message(
    id = UUID.randomUUID().toString(),
    chatId = "admin_support",
    isUser = true,
    content = "Help please!",
    timestamp = Timestamp.now(),
    senderUid = currentUser.uid,
    senderName = "John",
    senderRole = "user"
)

// Saved to Realtime DB
database
  .child("messages")
  .child(currentUser.uid)          // User's UID
  .child("admin_support")          // Chat ID
  .child(message.id)               // Message ID
  .setValue(message.toMap())

// Update admin inbox
firestore
  .collection("admin_inbox")
  .document(currentUser.uid)
  .set({
    userId: currentUser.uid,
    userName: "John",
    lastMessage: "Help please!",
    lastMessageTime: timestamp,
    unreadCount: +1,
    lastMessageFromUser: true
  })
```

### Admin Sending Reply

```kotlin
// Admin sends reply
val message = Message(
    id = UUID.randomUUID().toString(),
    chatId = "admin_support",
    isUser = false,              // KEY: Admin message
    content = "How can I help?",
    timestamp = Timestamp.now(),
    senderUid = admin.uid,
    senderName = "Support Team",
    senderRole = "admin"
)

// Saved to SAME location (user's collection)
database
  .child("messages")
  .child(selectedUserUID)        // User's UID (not admin's!)
  .child("admin_support")
  .child(message.id)
  .setValue(message.toMap())

// Mark as replied
firestore
  .collection("admin_inbox")
  .document(selectedUserUID)
  .update({
    unreadCount: 0,
    lastMessageFromUser: false
  })
```

### Real-Time Synchronization

Both user and admin subscribe to the same location:

```kotlin
// User subscribes
database
  .child("messages")
  .child(currentUser.uid)        // Own messages location
  .child("admin_support")
  .addValueEventListener(listener)

// Admin subscribes
database
  .child("messages")
  .child(selectedUserUID)        // User's messages location
  .child("admin_support")
  .addValueEventListener(listener)

// Result: Both see same messages in real-time
```

---

## 🎨 UI Components

### Message Bubbles

**User Message (Blue, Right-aligned)**
```
                            John
                     [message text]
                          12:30
```

**Admin Message (Gray, Left-aligned)**
```
Support Team (admin)
[message text]
12:31
```

### Admin Inbox Item

```
┌─────────────────────────────────────┐
│ [Avatar] John Doe                   │
│          john@example.com           │
│          Last message... 5 min ago  │
│                              [5]    │  ← unread badge
└─────────────────────────────────────┘
```

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        USER SIDE                            │
├─────────────────────────────────────────────────────────────┤
│  MainActivity -> "Admin Support" button                     │
│          ↓                                                   │
│  AdminChatActivity (opens)                                 │
│          ↓                                                   │
│  1. getOrCreateAdminChat() ← Firestore                     │
│  2. getAdminMessagesRealtime() ← Realtime DB               │
│          ↓                                                   │
│  Message List displayed (ChatAdapter)                      │
│          ↓                                                   │
│  sendAdminMessage() → Realtime DB → Admin sees in real-time│
└─────────────────────────────────────────────────────────────┘
                           ↕ (Real-time sync)
┌─────────────────────────────────────────────────────────────┐
│                        ADMIN SIDE                           │
├─────────────────────────────────────────────────────────────┤
│  Admin Login                                                │
│          ↓                                                   │
│  Admin Inbox (AdminInboxActivity)                          │
│          ↓                                                   │
│  getAdminInboxRealtime() ← Firestore (admin_inbox)        │
│          ↓                                                   │
│  Show unread conversations                                 │
│          ↓                                                   │
│  Click on user → AdminDashboardActivity                    │
│          ↓                                                   │
│  getAdminMessagesForUserRealtime() ← Realtime DB           │
│          ↓                                                   │
│  Show all messages from user                               │
│          ↓                                                   │
│  sendAdminReply() → Realtime DB → User sees in real-time   │
│  markConversationAsReplied() → Firestore                   │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Features Implemented

- ✅ Real-time message synchronization
- ✅ User-to-Admin messaging
- ✅ Admin-to-User messaging
- ✅ Message timestamps
- ✅ Sender identification (name + role)
- ✅ Admin inbox with unread count
- ✅ Conversation metadata tracking
- ✅ Multiple user support for admin
- ✅ Realtime message display (like Messenger)
- ✅ Auto-scroll to latest message
- ✅ Mark conversation as replied

---

## 📝 TODO / Future Enhancements

- [ ] Admin role verification (only admins can access AdminDashboardActivity)
- [ ] Typing indicators ("User is typing...")
- [ ] Read receipts (message seen/unseen)
- [ ] User avatars
- [ ] Message search
- [ ] Delete/Edit messages
- [ ] Message reactions/emojis
- [ ] File/Image sharing
- [ ] Admin assignment to specific user
- [ ] Conversation categories/tags
- [ ] Automated replies (out of office)
- [ ] Chat history export

---

## 🔐 Security Rules (Firestore)

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
        ".write": "root.child('users').child(auth.uid).child('role').val() == 'admin'"
      }
    }
  }
}
```

---

## 🔐 Security Rules (Realtime Database)

```json
{
  "rules": {
    "messages": {
      "$uid": {
        "admin_support": {
          ".read": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'",
          ".write": "auth.uid == $uid || root.child('users').child(auth.uid).child('role').val() == 'admin'"
        }
      }
    }
  }
}
```

---

## 🧪 Testing Checklist

- [ ] User can send message to admin
- [ ] Admin receives notification in inbox
- [ ] Admin can see user's message in real-time
- [ ] Admin can reply to user
- [ ] User receives admin's reply in real-time
- [ ] Timestamps display correctly
- [ ] Sender names display correctly
- [ ] Unread count updates correctly
- [ ] Multiple users can chat with admin simultaneously
- [ ] Messages persist after app restart
- [ ] Network disconnect/reconnect works smoothly

---

## 📞 Support

For questions or issues, refer to the inline code comments or contact the development team.

Last Updated: December 2024

