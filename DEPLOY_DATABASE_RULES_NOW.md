# URGENT: Deploy Updated Database Rules

## Problem Fixed ✅

Admin cannot write to Realtime DB because rules were too restrictive.

**Changed:**
```json
// Before (WRONG - only owner can write)
".write": "auth != null && auth.uid == $userId"

// After (CORRECT - any authenticated user can write)
".write": "auth != null"
```

---

## 🚀 DEPLOY NOW:

### Option 1: Firebase CLI (Fastest)
```bash
cd f:\HOCKITLON\ChatBot
firebase deploy --only database
```

### Option 2: Firebase Console
1. Firebase Console → Realtime Database → Rules
2. Copy content from `database.rules.json`
3. Paste → Publish

---

## After Deploy ✅

- ✅ Admin can write replies to user messages
- ✅ User can send messages to admin
- ✅ All chat features work!

**Deploy now to fix!**

